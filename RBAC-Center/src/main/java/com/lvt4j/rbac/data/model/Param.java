package com.lvt4j.rbac.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.context.annotation.Description;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.Unique;

/**
 * 配置项
 * @author LV
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("param")
@Description("配置项")
public class Param extends Model{
    
    @Col(id=true, autoId=true)
    public Integer autoId;
    
    @Unique(seq=0)
    public int proAutoId;
    
    @Like
    @Unique(seq=1)
    public String key;
    
    @Like
    public String name;
    
    public String des;
    
    public Integer seq;
    
    @NotCol
    public String val;
}
