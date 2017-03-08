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
 * 角色
 * @author LV
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("role")
public class Role extends Model{

    @Col(id=true, autoId=true)
    public Integer autoId;
    
    @Unique(seq=0)
    public int proAutoId;
    
    @Like
    @Unique(seq=1)
    public String id;
    
    @Like
    public String name;
    
    public String des;
    
    public Integer seq;
    
    
    @NotCol
    public List<Access> accesses;
    @NotCol
    public List<Permission> permissions;
}
