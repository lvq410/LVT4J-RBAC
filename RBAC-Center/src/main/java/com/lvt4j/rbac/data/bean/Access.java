package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.BaseModel;

/**
 * 访问项
 * @author LV
 */
@Table("access")
public class Access extends BaseModel {
    
    @Unique
    public int proAId;
    
    @Like
    public String pattern;
    
    @Like
    public String name;
    
}
