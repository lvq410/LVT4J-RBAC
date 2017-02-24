package com.lvt4j.rbac.data;

import java.util.Map;
import java.util.Set;

/**
 * 用户的在某一产品的所有权限信息
 * @author LV
 */
public class UserAuth{

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
    
}
