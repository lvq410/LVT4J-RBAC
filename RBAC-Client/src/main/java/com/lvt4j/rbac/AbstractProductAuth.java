package com.lvt4j.rbac;

import java.util.Set;

/**
 * 产品下用户的配置项、访问项、授权项<br>
 * 抽象类,具体实现需要采用懒加载机制
 * @author LV
 */
abstract class AbstractProductAuth{
    
    /** 产品id */
    String proId;
    /** 用户配置项缓存 */
    LruCache<String, UserAuth> userAuths;
    
    protected AbstractProductAuth(String proId, int cacheCapacity){
        this.proId = proId;
        userAuths = new LruCache<String, UserAuth>(cacheCapacity);
    }
    
    public UserAuth getUserAuth(String userId){
        UserAuth userAuth = userAuths.get(userId);
        if(userAuth!=null) return userAuth;
        synchronized (userAuths) {
            userAuth = userAuths.get(userId);
            if(userAuth!=null) return userAuth;
            userAuth = loadUserAuth(userId);
            userAuths.put(userId, userAuth);
        }
        return userAuth;
    }
    
    /** 加载用户配置项 */
    protected abstract UserAuth loadUserAuth(String userId);
    
    /** 用户是否有权限访问指定uri */
    public boolean allowAccess(String userId, String uri) {
        Set<String> access = getUserAuth(userId).access;
        for(String pattern : access)
            if(uri.matches(pattern)) return true;
        return false;
    }
    /** 用户是否有指定授权项的权限 */
    public boolean permit(String userId, String permissionId) {
        return getUserAuth(userId).permission.contains(permissionId);
    }
    
    public void clear() {
        userAuths.clear();
    }
    
}