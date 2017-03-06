package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.Unique;
import com.lvt4j.rbac.data.base.BaseModel;

/**
 * 产品
 * @author LV
 */
@Table("product")
public class Product extends BaseModel {
    
    @Like
    @Unique
    public String id;
    
    public long lastModify;
}
