package com.lvt4j.rbac;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.management.ObjectName;

import lombok.Getter;
import lombok.SneakyThrows;

/**
 * 产品下用户的配置项、访问项、授权项<br>
 * 抽象类,具体实现需要采用懒加载机制
 * @author LV
 */
abstract class AbstractProductAuth implements AbstractProductAuthMBean,Closeable{
    private static final Logger log = Logger.getLogger(AbstractProductAuth.class.getName());
    
    private static final UserAuth EmptyAuth = new UserAuth();
    static{
        setAsVisitorAuth(EmptyAuth, null);
    }
    
    /** 产品id */
    @Getter
    protected final String proId;
    /** 用户权限缓存 */
    private final ProductAuthCache cache;
    private final ObjectName objectName;
    
    /** 游客权限 */
    protected UserAuth visitorAuth;
    
    /** 加载成功次数 */
    private final AtomicLong loadSuccCount = new AtomicLong();
    /** 加载失败次数 */
    private final AtomicLong loadFailCount = new AtomicLong();
    
    protected AbstractProductAuth(String proId, ProductAuthCache cache){
        this.proId = proId;
        this.cache = cache;
        objectName = objectName();
        cache.setLoader(this::innerLoad);
        
        try{
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
        }catch(Exception e){
            log.log(SEVERE, "ProductAuth注册JMX异常", e);
        }
    }
    @SneakyThrows
    private ObjectName objectName() {
        Hashtable<String, String> kvs = new Hashtable<>();
        kvs.put("type", getClass().getSimpleName());
        kvs.put("proId", proId);
        kvs.put("hash", String.valueOf(hashCode()));
        return new ObjectName("RbacProductAuth", kvs);
    }
    
    /** 查询用户权限,若userId为空,或用户未在权限中心注册,返回游客权限 */
    public UserAuth getUserAuth(String userId){
        if(userId==null || userId.isEmpty()){ //用户ID为空,尝试返回游客权限
            UserAuth visitorAuth = getVisitorAuth();
            return visitorAuth!=null?visitorAuth:EmptyAuth; //如果游客权限加载失败,返回空权限
        }
        UserAuth userAuth = cache.getOrLoad(userId);
        if(userAuth!=null) return userAuth;
        //用户权限加载失败,尝试返回游客权限
        userAuth = new UserAuth();
        userAuth.userId = userId;
        return setAsVisitorAuth(userAuth, getVisitorAuth());
    }
    /** 加载后处理了未注册用户覆盖为游客权限的逻辑 */
    private UserAuth innerLoad(String userId) {
        UserAuth userAuth = loadWithStatis(userId);
        if(userAuth==null) return null;
        if(!userAuth.exist){ //用户未在权限中心注册,权限中心会返回空权限，此时覆盖为游客权限
            UserAuth visitorAuth = getVisitorAuth();
            if(visitorAuth==null) return null; //游客权限加载失败,返回空
            setAsVisitorAuth(userAuth, visitorAuth);
        }
        return userAuth;
    }
    
    /** 查询游客权限,若未加载游客权限,则加载,若加载失败,返回null */
    protected UserAuth getVisitorAuth() {
        UserAuth visitorAuth = this.visitorAuth;
        if(visitorAuth!=null) return visitorAuth;
        synchronized(this){
            visitorAuth = this.visitorAuth;
            if(visitorAuth!=null) return visitorAuth;
            visitorAuth = loadWithStatis(null);
            if(visitorAuth==null) return null;
            if(isUserAuthNull(visitorAuth)){ //权限数据为空,说明产品不存在了,打印警告并覆盖为空权限
                log.warning(String.format("产品[%s]未在权限中心注册", proId));
                setAsVisitorAuth(visitorAuth, null);
            }
            return this.visitorAuth = visitorAuth;
        }
    }
    /** 包装loadUserAuth，统计加载成功和失败次数 */
    private UserAuth loadWithStatis(String userId) {
        UserAuth userAuth = loadUserAuth(userId);
        (userAuth!=null?loadSuccCount:loadFailCount).incrementAndGet();
        return userAuth;
    }
    
    /** 加载用户权限,若userId为空,加载游客权限,若加载失败,返回null */
    protected abstract UserAuth loadUserAuth(String userId);
    
    protected static UserAuth setAsVisitorAuth(UserAuth userAuth, UserAuth visitorAuth) {
        if(visitorAuth==null){
            userAuth.params = Collections.emptyMap();
            userAuth.roles = Collections.emptySet();
            userAuth.accesses = Collections.emptySet();
            userAuth.permissions = Collections.emptySet();
        }else{
            userAuth.params = visitorAuth.params;
            userAuth.roles = visitorAuth.roles;
            userAuth.accesses = visitorAuth.accesses;
            userAuth.permissions = visitorAuth.permissions;
        }
        return userAuth;
    }
    protected static boolean isUserAuthNull(UserAuth auth){
        return auth.params==null && auth.roles==null && auth.accesses==null && auth.permissions==null;
    }
    
    /** 用户是否有权限访问指定uri */
    public boolean allowAccess(String userId, String uri) {
        return getUserAuth(userId).allowAccess(uri);
    }
    /** 用户是否有指定授权项的权限 */
    public boolean permit(String userId, String permissionId) {
        return getUserAuth(userId).permit(permissionId);
    }
    
    /** 缓存设定容量 */
    public int getCapacity() { return cache.getCapacity(); }
    /** 缓存当前容量 */
    public int getSize() { return cache.getSize(); }
    /** 缓存命中数 */
    public long getHitCount() { return cache.getHitCount(); }
    /** 缓存未命中数 */
    public long getMissCount() { return cache.getMissCount(); }
    /** 加载成功次数 */
    public long getLoadSuccCount() { return loadSuccCount.get(); }
    /** 加载失败次数 */
    public long getLoadFailCount() { return loadFailCount.get(); }
    
    /** 立刻清理掉指定用户缓存，为null表示清理所有缓存 */
    public void invalidate(String userId) {
        if(log.isLoggable(FINEST)) log.finest(String.format("清理缓存:%s", userId));
        if(userId==null || userId.isEmpty()) visitorAuth = null; //null时顺带清理游客缓存
        cache.invalidate(null);
    }
    /** 异步重载指定用户缓存，为null表示重载已缓存的所有用户的缓存 */
    public void invalidateAsync(String userId) {
        if(log.isLoggable(FINEST)) log.finest(String.format("异步重载缓存:%s", userId));
        if(userId==null || userId.isEmpty()) visitorAuth = null; //null时顺带清理游客缓存
        cache.invalidateAsync(userId);
    }
    
    @Override
    public void close() throws IOException {
        if(objectName!=null){
            try{
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
            }catch(Exception e){
                log.log(SEVERE, "ProductAuth注销JMX异常", e);
            }
        }
        cache.close();
    }
    
}