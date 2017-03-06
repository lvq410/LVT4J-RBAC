package com.lvt4j.rbac.data.bean;

import java.util.List;

import lombok.Data;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.web.controller.EditController.ColLike;

/**
 * 角色
 * @author LV
 */
@Data
@Table("role")
public class Role {
    
    @Col(id=true, autoId=true)
    public Integer aId;
    
    public int proAId;
    
    @ColLike
    public String id;
    
    @ColLike
    public String name;
    
    public String des;
    
    public Integer seq;
    
    @NotCol
    public List<Access> accesses;
    @NotCol
    public List<Permission> permissions;
}
