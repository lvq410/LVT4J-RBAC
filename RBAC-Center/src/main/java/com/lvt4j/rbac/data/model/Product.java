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
 * 产品
 * @author LV
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("product")
@Description("产品")
public class Product extends Model{
    
    @Col(id=true, autoId=true)
    public Integer autoId;
    
    @Like
    @Unique
    public String id;
    
    @Like
    public String name;
    
    public String des;
    
    public long lastModify;
    
    public Integer seq;
}
