package com.lvt4j.rbac.data.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.Unique;

/**
 * 用户
 * @author LV
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("user")
public class User extends Model{
    
    @Col(id=true, autoId=true)
    public Integer autoId;
    
    @Like
    @Unique
    public String id;
    
    @Like
    public String name;
    
    public String des;
    
    public Integer seq;
    
    @NotCol
    public List<Param> params;
    @NotCol
    public List<Role> roles;
    @NotCol
    public List<Access> accesses;
    @NotCol
    public List<Permission> permissions;
}
