package com.lvt4j.rbac;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 用户的在某一产品的所有权限信息
 * @author LV
 */
public class UserAuth implements Serializable{

    public static final String ReqAttr = "rbac";
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    public String userId;
    /** 用户在授权中心是否存在 */
    public boolean exist;
    /** 用户的所有配置项 */
    public Map<String, String> param;
    /** 用户的所有角色 */
    public Set<String> roles;
    /** 用户的所有访问项 */
    public Set<String> access;
    /** 用户的所有授权项 */
    public Set<String> permission;
    
    /** 用户是否有权限访问指定uri */
    public boolean allowAccess(String uri) {
        for(String pattern : access)
            if(uri.matches(pattern)) return true;
        return false;
    }
    /** 用户是否有指定授权项的权限 */
    public boolean permit(String permissionId) {
        return permission.contains(permissionId);
    }
    
}
