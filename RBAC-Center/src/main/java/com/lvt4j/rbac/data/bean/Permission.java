package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.BaseModel;
/**
 * 授权项
 * @author LV
 */
@Table("permission")
public class Permission extends BaseModel{
    
    public int proAId;
    
    @Like
    public String id;
    
    @Like
    public String name;
    
}
