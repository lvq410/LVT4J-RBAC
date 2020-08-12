package com.lvt4j.rbac;

import static java.util.Arrays.asList;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.lvt4j.rbac.BroadcastMsg.CacheClean;
import com.lvt4j.rbac.BroadcastMsg.Handshake;

import lombok.Getter;

/**
 * 从权限中心获取指定项目的用户权限的客户端<br>
 * 内置缓存功能<br>
 * 不用时切记销毁{@link #close()}
 * @author LV on 2020年8月3日
 */
public class ProductAuthClient extends AbstractProductAuth implements ProductAuthClientMBean{
    private static final Logger log = Logger.getLogger(ProductAuthClient.class.getName());
    
    private static final AtomicInteger ClientIder = new AtomicInteger();
    
    /** 默认产品用户权限缓存容量:0即不限 */
    public static final int CacheCapacityDef = 0;
    /** 默认与授权中心连接的协议:http */
    public static final String RbacCenterProtocolDef = "http";
    /** 默认授权中心地址:127.0.0.1:80 */
    public static final String RbacCenterAddrDef = "127.0.0.1:80";
    /** 默认从授权中心加载数据超时时间:200ms */
    public static final int RbacCenterTimeoutDef = 200;
    
    /** 版本号 */
    private static final String Version = loadVersion();
    private static String loadVersion() {
        InputStream is = ProductAuthClient.class.getResourceAsStream("version");
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.readLine();
        }catch(Throwable e){
            log.log(SEVERE, "加载版本号失败",e);
            return "unknown";
        }finally{
            try{
                is.close();
            }catch(Throwable e){
                log.log(WARNING, "关闭加载版本号的流失败",e);
            }
        }
    }
    
    /** 授权中心订阅的接口地址 */
    private static final String Path_Sub = "/inner/v2/subscribe";
    /** 加载用户权限的接口路径 */
    private static final String Path_UserAuth = "/inner/v2/userAuth";
    
    
    /** 客户端ID，由本机IP:产品ID:进程ID:递增数 构成 */
    @Getter
    private final String clientId;
    
    /** 权限中心根地址 */
    @Getter
    private final String rbacCenter;
    /** 产品同步的URL */
    private final String url_Sub;
    /** 加载用户权限的URL */
    private final String url_UserAuth;
    
    /** 从授权中心加载数据超时时间 */
    @Getter
    private final int rbacCenterTimeoutInMillis;
    
    private final CenterSubscriber subscriber;
    
    
    /**
     * 构造获取权限的客户端，默认无限缓存<br>
     * 不用时切记销毁{@link #close()}
     * @param proId 产品ID
     * @param rbacCenterAddr 授权中心地址 host[:port] 格式
     * @see #ProductAuthClient(String, int, String, String, long)
     */
    public ProductAuthClient(String proId, String rbacCenterAddr){
        this(proId, CacheCapacityDef,
            RbacCenterProtocolDef, rbacCenterAddr, RbacCenterTimeoutDef);
    }
    /**
     * 构造获取权限的客户端，默认无限缓存<br>
     * 不用时切记销毁{@link #close()}
     * @param proId 产品ID
     * @param rbacCenterAddr 授权中心地址 host[:port] 格式
     * @param rbacCenterTimeoutInMillis 从授权中心加载数据超时时间
     * @see #ProductAuthClient(String, int, String, String, long)
     */
    public ProductAuthClient(String proId, String rbacCenterAddr, int rbacCenterTimeoutInMillis){
        this(proId, CacheCapacityDef,
            RbacCenterProtocolDef, rbacCenterAddr, rbacCenterTimeoutInMillis);
    }
    /**
     * 构造获取权限的客户端<br>
     * 不用时切记销毁{@link #close()}
     * @param proId 产品ID
     * @param capacity 缓存容量，0为不限
     * @param rbacCenterProtocol 与授权中心连接的协议:http/https
     * @param rbacCenterAddr 授权中心地址 host[:port] 格式
     * @param rbacCenterTimeoutInMillis 从授权中心加载数据超时时间
     */
    public ProductAuthClient(String proId, int capacity,
            String rbacCenterProtocol, String rbacCenterAddr, int rbacCenterTimeoutInMillis){
        this(proId, capacity==0?new ConcurrentHashMapProductAuthCache():new LruLinkedHashMapProductAuthCache(capacity),
            rbacCenterProtocol, rbacCenterAddr, rbacCenterTimeoutInMillis);
    }
    /**
     * 构造获取权限的客户端<br>
     * 给定了用户权限缓存实现<br>
     * 不用时切记销毁{@link #close()}
     * @param proId
     * @param cache 缓存实现
     * @param rbacCenterProtocol
     * @param rbacCenterAddr
     * @param rbacCenterTimeoutInMillis
     */
    public ProductAuthClient(String proId, ProductAuthCache cache,
            String rbacCenterProtocol, String rbacCenterAddr, int rbacCenterTimeoutInMillis){
        super(proId, cache);
        if(!asList("http","https").contains(rbacCenterProtocol.toLowerCase()))
            throw new IllegalArgumentException(String.format("不支持的协议:%s", rbacCenterProtocol));
        
        rbacCenter = rbacCenterProtocol+"://"+rbacCenterAddr;
        url_Sub = rbacCenter+Path_Sub;
        url_UserAuth = rbacCenter+Path_UserAuth;
        this.rbacCenterTimeoutInMillis = rbacCenterTimeoutInMillis;
        String host = null;
        try{
            host = Inet4Address.getLocalHost().getHostAddress();
        }catch(Exception e){
            log.log(SEVERE, "获取本机IP异常", e);
            host = "unknownhost";
        }
        String pid = null;
        try{
            pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        }catch (Throwable e) {
            log.log(SEVERE, "获取进程ID异常", e);
            pid = "unknownpid";
        }
        clientId = host+":"+proId+":"+pid+":"+ClientIder.incrementAndGet();
        
        subscriber = new CenterSubscriber();
    }

    @Override
    protected UserAuth loadUserAuth(String userId) {
        if(log.isLoggable(FINEST)){
            if(userId!=null && !userId.isEmpty()){
                log.log(FINEST, String.format("从授权中心加载产品[%s]用户[%s]权限", proId, userId));
            }else{
                log.log(FINEST, String.format("从授权中心加载产品[%s]游客权限", proId));
            }
        }
        HttpURLConnection cnn = null;
        InputStream in = null;
        try{
            String userAuthUrl = url(url_UserAuth, "proId", proId, "userId", userId, "clientId", clientId);
            cnn = (HttpURLConnection) new URL(userAuthUrl).openConnection();
            cnn.setConnectTimeout(rbacCenterTimeoutInMillis);
            cnn.setReadTimeout(rbacCenterTimeoutInMillis);
            cnn.connect();
            in = cnn.getInputStream();
            GZIPInputStream zipIn = new GZIPInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(zipIn);
            return (UserAuth) ois.readObject();
        }catch(Throwable e){
            if(userId!=null && !userId.isEmpty()){
                log.log(SEVERE, String.format("从授权中心加载产品[%s]用户[%s]权限失败", proId, userId), e);
            }else{
                log.log(SEVERE, String.format("从授权中心加载产品[%s]游客权限失败", proId, userId), e);
            }
            return null;
        }finally {
            try{
                if(in!=null) in.close();
            }catch(Exception ignore){
                log.log(WARNING, "输入流关闭异常", ignore);
            }
            try{
                if(cnn!=null) cnn.disconnect();
            }catch(Exception ignore){
                log.log(WARNING, "链接关闭异常", ignore);
            }
        }
    }
    
    private String url(String uri, Object... params) {
        StringBuilder url = new StringBuilder(uri).append('?');
        for(int i=0; i<params.length; i++){
            try{
                if(params[i+1]==null) {
                    i+=1;
                    continue;
                }
                if(i!=0) url.append('&');
                url.append(params[i++]).append('=').append(URLEncoder.encode(params[i].toString(),"utf8"));
            }catch(UnsupportedEncodingException ig){}
        }
        return url.toString();
    }
    
    class CenterSubscriber extends Thread {
        
        private long masterTerm;
        private long broadMsgIdx;
        
        private volatile boolean destory;
        
        public CenterSubscriber() {
            setName(proId+"-RbacCenterSubscriber");
            start();
        }
        
        @Override
        public void run() {
            if(log.isLoggable(FINEST)) log.finest("CenterSubscriber启动");
            while(!destory){
                try{
                    subCenter();
                }catch(Throwable e){
                    if(destory) return;
                    log.log(WARNING, "与授权中心的连接断开", e);
                }
                if(destory) return;
                try{
                    Thread.sleep(10000);
                }catch(Exception ig){}
            }
            if(log.isLoggable(FINEST)) log.finest("CenterSubscriber退出");
        }
        private void subCenter() throws Throwable {
            String url = url(url_Sub, "proId", proId, "clientId", clientId, "version", Version);
            HttpURLConnection cnn = null;
            InputStream in = null;
            try{
                cnn = (HttpURLConnection) new URL(url).openConnection();
                cnn.setConnectTimeout(rbacCenterTimeoutInMillis);
                in = cnn.getInputStream();
                log.log(Level.INFO, String.format("连接授权中心[%s]成功", rbacCenter));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while(!destory){
                    line = reader.readLine();
                    if(log.isLoggable(FINEST)) log.log(FINEST, String.format("订阅原始消息:%s", line));
                    if(!line.startsWith("data:")) continue;
                    line = line.substring(5);
                    if(line.isEmpty()) continue;
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(line)));
                    Object msg;
                    try{
                        msg = ois.readObject();
                    }catch(Throwable e){
                        log.log(WARNING, String.format("非法的广播消息:%s", line), e);
                        continue;
                    }
                    if(log.isLoggable(FINEST)) log.log(FINEST, String.format("订阅消息:%s", msg));
                    try{
                        onMessage(msg);
                    }catch(Throwable e){
                        log.log(WARNING, String.format("处理广播消息异常:%s", msg), e);
                        continue;
                    }
                }
            }finally{
                try{
                    if(in!=null) in.close();
                }catch(Exception ignore){
                    log.log(WARNING, "输入流关闭异常", ignore);
                }
                try{
                    if(cnn!=null) cnn.disconnect();
                }catch(Exception ignore){
                    log.log(WARNING, "链接关闭异常", ignore);
                }
            }
        }
        
        public void onMessage(Object msg) throws Throwable {
            if(msg instanceof Handshake){
                onHandeshake((Handshake) msg);
            }else if (msg instanceof CacheClean) {
                onCacheClean((CacheClean) msg);
            }
        }
        /** 握手信号，初次建立连接 */
        private void onHandeshake(Handshake msg) {
            masterTerm = msg.masterTerm;
            broadMsgIdx = msg.msgIdx;
        }
        private void onCacheClean(CacheClean msg) {
            if(!Objects.equals(masterTerm, msg.masterTerm)){ //master变更
                log.info("授权中心主节点变更");
                masterTerm = msg.masterTerm;
                broadMsgIdx = msg.msgIdx;
                invalidateAsync(null);
                return;
            }
            long msgIdxDiff = msg.msgIdx-broadMsgIdx;
            if(msgIdxDiff>1 || msgIdxDiff<0){ //漏消息
                log.info("漏授权中心消息");
                masterTerm = msg.masterTerm;
                broadMsgIdx = msg.msgIdx;
                invalidateAsync(null);
                return;
            }
            
            masterTerm = msg.masterTerm;
            broadMsgIdx = msg.msgIdx;
            if(msg.proId!=null && !proId.equals(msg.proId)) return;
            
            if(log.isLoggable(FINEST)) log.finest(String.format("根据广播消息重载产品[%s]用户[%s]缓存", proId, msg.userId));
            invalidateAsync(msg.userId);
        }
        
        @Override
        public void destroy() {
            destory = true;
            interrupt();
            try{
                join(1000);
            }catch(InterruptedException ig){}
        }
    }
    
    @Override
    public void close() throws IOException {
        subscriber.destroy();
        super.close();
    }
    
}