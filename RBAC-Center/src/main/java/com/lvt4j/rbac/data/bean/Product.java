package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.web.controller.EditController.ColLike;

/**
 * 产品
 * @author LV
 */
@Data
@Table("product")
public class Product {
    @Col(id=true)
    @ColLike
    public String id;
    
    @ColLike
    public String name;
    
    public String des;
    
    public long lastModify;
}
