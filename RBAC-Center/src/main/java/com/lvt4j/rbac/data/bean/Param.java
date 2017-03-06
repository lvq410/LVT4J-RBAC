package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.base.ProCtrlModel;

/**
 * 配置项
 * @author LV
 */
@Table("param")
public class Param extends ProCtrlModel{
    
    @Like
    @Unique(seq=1)
    public String key;
    
    @NotCol
    public String val;
}
