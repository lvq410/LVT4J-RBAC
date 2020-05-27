package com.lvt4j.rbac.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.context.annotation.Description;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.Unique;

/**
 * 授权项
 * @author LV
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("permission")
@Description("授权项")
public class Permission extends Model{
    
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
}
