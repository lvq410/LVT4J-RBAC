package com.lvt4j.rbac.data.bean;

import java.util.List;

import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.Unique;
import com.lvt4j.rbac.data.base.BaseModel;

/**
 * 用户
 * @author LV
 */
@Table("user")
public class User extends BaseModel {
    
    @Like
    @Unique
    public String id;
    
    @NotCol
    public List<Param> params;
    @NotCol
    public List<Role> roles;
    @NotCol
    public List<Access> accesses;
    @NotCol
    public List<Permission> permissions;
}
