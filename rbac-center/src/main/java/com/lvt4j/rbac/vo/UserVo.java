package com.lvt4j.rbac.vo;

import java.util.List;

import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Role;
import com.lvt4j.rbac.po.User;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author LV on 2020年8月6日
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class UserVo extends User {

    public List<ParamVo> params;
    public List<Role> roles;
    public List<Access> accesses;
    public List<Permission> permissions;
    
}