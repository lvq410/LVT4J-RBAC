package com.lvt4j.rbac.data.bean;

import java.util.List;

import lombok.Data;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.web.controller.EditController.ColLike;

/**
 * 用户
 * @author LV
 */
@Data
@Table("user")
public class User {
    @Col(id=true)
    @ColLike
    public String id;
    
    @ColLike
    public String name;
    
    public String des;
    
    @NotCol
    public List<Param> params;
    @NotCol
    public List<Role> roles;
    @NotCol
    public List<Access> accesses;
    @NotCol
    public List<Permission> permissions;
}
