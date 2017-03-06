package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.base.ProCtrlModel;
/**
 * 授权项
 * @author LV
 */
@Table("permission")
public class Permission extends ProCtrlModel{
    
    @Like
    public String id;
}
