package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.BaseModel;

/**
 * 配置项
 * @author LV
 */
@Table("param")
public class Param extends BaseModel{
    
    @Unique(seq=0)
    public int proAId;
    
    @Like
    @Unique(seq=1)
    public String key;
    
    @Like
    public String name;
    
    @NotCol
    public String val;
}
