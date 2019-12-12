package com.lvt4j.rbac;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用户的在某一产品的所有权限信息
 * @author LV
 */
public class UserAuth implements Serializable{

    public static final String ReqAttr = "rbac";
    
    private static final long serialVersionUID = 1L;
    
    private static final Map<String, String> emptyMap = new HashMap<String, String>(0);
    private static final Set<String> emptySet = new HashSet<String>(0);
    
    /** 用户ID */
    public String userId;
    /** 用户名称 */
    public String userName;
    /** 用户描述 */
    public String userDes;
    /** 用户在授权中心是否存在 */
    public boolean exist;
    /** 用户的所有配置项 */
    public Map<String, String> params;
    /** 用户的所有角色 */
    public Set<String> roles;
    /** 用户的所有访问项 */
    public Set<String> accesses;
    /** 用户的所有授权项 */
    public Set<String> permissions;
    
    /** 用户是否有权限访问指定uri */
    public boolean allowAccess(String uri) {
        for(String pattern : accesses)
            if(uri.matches(pattern)) return true;
        return false;
    }
    /** 用户是否有指定授权项的权限 */
    public boolean permit(String permissionId) {
        return permissions.contains(permissionId);
    }
    
    void setAuth(UserAuth auth) {
        if(auth==null) {
            params = emptyMap;
            roles = emptySet;
            accesses = emptySet;
            permissions = emptySet;
        } else {
            params = auth.params;
            roles = auth.roles;
            accesses = auth.accesses;
            permissions = auth.permissions;
        }
    }
    
    boolean isEmpty(){
        return params==null
                && roles==null
                && accesses==null
                && permissions==null;
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
        StringBuilder str = new StringBuilder();
        str.append("userId:").append(userId).append('\n')
            .append("userName:").append(userName).append('\n')
            .append("userDes:").append(userDes).append('\n')
            .append("exist:").append(exist).append('\n')
            .append("params:").append(params).append('\n')
            .append("roles:").append(roles).append('\n')
            .append("accesses:").append(accesses).append('\n')
            .append("permissions:").append(permissions);
        return str.toString();
    }
}
