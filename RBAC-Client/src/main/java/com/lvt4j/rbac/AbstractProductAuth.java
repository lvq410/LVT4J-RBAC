package com.lvt4j.rbac;



/**
 * 产品下用户的配置项、访问项、授权项<br>
 * 抽象类,具体实现需要采用懒加载机制
 * @author LV
 */
abstract class AbstractProductAuth{
    
    /** 产品id */
    protected String proId;
    /** 用户配置项缓存 */
    protected LruCache<String, UserAuth> userAuths;
    /** 游客权限 */
    protected UserAuth visitorAuth;
    
    protected AbstractProductAuth(String proId, int cacheCapacity){
        if(proId==null)return;
        this.proId = proId;
        userAuths = new LruCache<String, UserAuth>(cacheCapacity);
    }
    
    /** 查询用户权限,若userId为空,或用户未在权限中心注册,返回游客权限 */
    public UserAuth getUserAuth(String userId){
        if(userId==null || userId.isEmpty()){ //用户ID为空,尝试返回游客权限
            UserAuth visitorAuth = getVisitorAuth();
            if(visitorAuth!=null) return visitorAuth;
            UserAuth userAuth = new UserAuth(); //游客权限加载失败,返回空权限
            userAuth.exist = false;
            userAuth.setAuth(null);
            return userAuth;
        }
        UserAuth userAuth = userAuths.get(userId);
        if(userAuth!=null) return userAuth;
        synchronized (this) {
            userAuth = userAuths.get(userId);
            if(userAuth!=null) return userAuth;
            userAuth = loadUserAuth(userId);
            if(userAuth==null) { //用户权限加载失败,尝试返回游客权限,并且不缓存用户权限
                userAuth = new UserAuth();
                userAuth.userId = userId;
                userAuth.exist = false;
                userAuth.setAuth(getVisitorAuth());
                return userAuth;
            }
            if(!userAuth.exist){ //用户未在权限中心注册,覆盖为游客权限
                UserAuth visitorAuth = getVisitorAuth();
                if(visitorAuth==null) { //游客权限加载失败,返回空权限,并且不缓存用户权限
                    userAuth.setAuth(null);
                    return userAuth;
                }
                userAuth.setAuth(visitorAuth);
            }
            userAuths.put(userId, userAuth);
        }
        return userAuth;
    }
    
    /** 查询游客权限,若未加载游客权限,则加载,若加载失败,返回null */
    protected UserAuth getVisitorAuth() {
        UserAuth visitorAuth = this.visitorAuth;
        if(visitorAuth!=null) return visitorAuth;
        synchronized (this) {
            visitorAuth = this.visitorAuth;
            if(visitorAuth!=null) return visitorAuth;
            visitorAuth = loadUserAuth(null);
            if(visitorAuth==null) return null;
            if(visitorAuth.isEmpty()){ //权限数据为空,说明产品不存在了,打印警告并覆盖为空权限
                System.err.println("产品["+proId+"]未在权限中心注册!");
                visitorAuth.setAuth(null);
            }
            this.visitorAuth = visitorAuth;
            return this.visitorAuth;
        }
    }
    
    /** 加载用户权限,若userId为空,加载游客权限,若加载失败,返回null */
    protected abstract UserAuth loadUserAuth(String userId);
    
    /** 用户是否有权限访问指定uri */
    public boolean allowAccess(String userId, String uri) {
        return getUserAuth(userId).allowAccess(uri);
    }
    /** 用户是否有指定授权项的权限 */
    public boolean permit(String userId, String permissionId) {
        return getUserAuth(userId).permit(permissionId);
    }
    
    public void clear() {
        userAuths.clear();
        visitorAuth = null;
    }
    
    public void destory() {}
    
}