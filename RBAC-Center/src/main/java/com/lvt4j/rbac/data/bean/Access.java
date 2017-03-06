package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Like;
import com.lvt4j.rbac.data.base.ProCtrlModel;

/**
 * 访问项
 * @author LV
 */
@Table("access")
public class Access extends ProCtrlModel {
    
    @Like
    public String pattern;
    
}
