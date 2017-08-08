package com.lvt4j.rbac;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

public class ProductAuth4Client extends AbstractProductAuth{

    /** 产品同步的接口路径 */
    private static final String Path_ProLastModify = "/inner/proLastModify";
    /** 加载用户权限的接口路径 */
    private static final String Path_UserAuth = "/inner/userAuth";
    
    /** 产品同步的URL */
    private final String Url_ProLastModify;
    /** 加载用户权限的URL */
    private final String Url_UserAuth;
    
    /** 上传同步记录的该产品最近修改时间 */
    private long lastModify = 0;
    /** 与授权中心的同步定时器 */
    private Timer rbacCenterSyncTimer;
    /** 与授权中心的同步任务,若同步数据中最近修改时间不符,则清空缓存 */
    private TimerTask rbacCenterSync = new TimerTask(){
        @Override
        public void run(){
            try {
                Map<String, Object> rst = loadInner(Url_ProLastModify);
                Long lastModify = (Long) rst.get("lastModify");
                if(lastModify==null){
                    System.err.println("与授权中心同步失败!原因为:产品最近修改时间不能为空!");
                    return;
                }
                if(lastModify!=ProductAuth4Client.this.lastModify) {
                    ProductAuth4Client.this.lastModify = lastModify;
                    clear();
                }
            } catch (Throwable e) {
                System.err.println("与授权中心同步失败!原因为:"+e.getMessage());
            }
        }
    };
    /** 与授权中心的同步超时时间 */
    private final int RbacCenterSyncTimeout;
    
    public ProductAuth4Client(String proId, int capacity,
            String rbacCenterProtocol, String rbacCenterAddr, int rbacCenterSyncInterval, int rbacCenterSyncTimeout){
        super(proId, capacity);
        Url_ProLastModify = rbacCenterProtocol+"://"+rbacCenterAddr+Path_ProLastModify+"?proId="+proId;
        Url_UserAuth = rbacCenterProtocol+"://"+rbacCenterAddr+Path_UserAuth;
        rbacCenterSyncTimer = new Timer();
        rbacCenterSyncTimer.schedule(rbacCenterSync, 0, rbacCenterSyncInterval*60*1000);
        RbacCenterSyncTimeout = rbacCenterSyncTimeout;
    }
    public ProductAuth4Client(String proId, String rbacCenterAddr){
        this(proId, RbacBaseFilter.CacheCapacityDef,
                RbacBaseFilter.RbacCenterProtocolDef, rbacCenterAddr,
                RbacBaseFilter.RbacCenterSyncIntervalDef, RbacBaseFilter.RbacCenterSyncTimeoutDef);
    }

    @Override
    protected UserAuth loadUserAuth(String userId) {
        try {
            String userAuthUrl = Url_UserAuth+"?proId="+URLEncoder.encode(proId, RbacBaseFilter.Encoding);
            if(userId!=null && !userId.isEmpty()) userAuthUrl+="&userId="+URLEncoder.encode(userId, RbacBaseFilter.Encoding);
            Map<String, Object> rst = loadInner(userAuthUrl);
            Long lastModify = (Long) rst.get("lastModify");
            if(lastModify==null) throw new IllegalArgumentException("加载结果中产品最近同步时间不能为null!");
            if(lastModify!=this.lastModify) {
                this.lastModify = lastModify;
                clear();
            }
            UserAuth userAuth = (UserAuth) rst.get("userAuth");
            if(userAuth==null) throw new IllegalArgumentException("加载结果中用户权限不能为null!");
            return userAuth;
        } catch (Throwable e) {
            System.err.println("从授权中心加载"
                    +(((userId!=null && !userId.isEmpty())?("用户["+userId+"]"):"游客"))
                    +"权限失败!:"+e.getMessage());
            return null;
        }
    }
    
    @Override
    public void destory(){
        rbacCenterSyncTimer.cancel();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadInner(String spec) throws Exception {
        URL url = null;
        try {
            url = new URL(spec);
        } catch (MalformedURLException ignore) {}
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(RbacCenterSyncTimeout);
            conn.setReadTimeout(RbacCenterSyncTimeout);
            conn.connect();
        } catch (IOException e) {
            try {
                if(conn!=null) conn.disconnect();
            } catch (Exception ignore) {}
            throw e;
        }
        InputStream in = null;
        try {
            in = conn.getInputStream();
            GZIPInputStream zipIn = new GZIPInputStream(in);
            ObjectInputStream ois = new ObjectInputStream(zipIn);
            return (Map<String, Object>) ois.readObject();
        } finally {
            try {
                if(in!=null) in.close();
            } catch (Exception ignore) {}
            try {
                if(conn!=null) conn.disconnect();
            } catch (Exception ignore) {}
        }
    }
    
}
