package com.lvt4j.rbac;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 用户的在某一产品的所有权限信息
 * @author LV
 */
public class UserAuth implements Serializable{
    private static final long serialVersionUID = 1L;

    public static final String ReqAttr = "rbac";
    
    /** 用户ID，null代表游客 */
    public String userId;
    /** 用户名称，可为null */
    public String userName;
    /** 用户描述，可为null */
    public String userDes;
    /** 用户在授权中心是否存在 */
    public boolean exist;
    /** 用户的所有配置项，不会为null，但可能为空map */
    public Map<String, String> params;
    /** 用户的所有角色，不会为null，但可能为空Set */
    public Set<String> roles;
    /** 用户的所有访问项，不会为null，但可能为空Set */
    public Set<String> accesses;
    /** 用户的所有授权项，不会为null，但可能为空Set */
    public Set<String> permissions;
    
    /** 用户是否有权限访问指定uri */
    public boolean allowAccess(String uri) {
        for(String pattern : accesses)
            if(uri.matches(pattern)) return true;
        return false;
    }
    /** 用户是否有指定授权项的权限 */
    public boolean permit(String permission) {
        for(String pattern : permissions)
            if(permission.matches(pattern)) return true;
        return false;
    }
    
    public UserAuth() {}
    public String getUserId(){return userId;}
    public String getUserName(){return userName;}
    public String getUserDes(){return userDes;}
    public boolean isExist(){return exist;}
    public Map<String, String> getParams(){return params;}
    public Set<String> getRoles(){return roles;}
    public Set<String> getAccesses(){return accesses;}
    public Set<String> getPermissions(){return permissions;}
    
    @Override
    public String toString(){
        return new StringBuilder().append("userId:").append(userId).append('\n')
            .append("userName:").append(userName).append('\n')
            .append("userDes:").append(userDes).append('\n')
            .append("exist:").append(exist).append('\n')
            .append("params:").append(params).append('\n')
            .append("roles:").append(roles).append('\n')
            .append("accesses:").append(accesses).append('\n')
            .append("permissions:").append(permissions)
            .toString();
    }
}