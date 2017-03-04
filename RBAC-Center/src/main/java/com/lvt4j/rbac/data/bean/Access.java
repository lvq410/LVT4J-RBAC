package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.web.controller.EditController.ColLike;

/**
 * 访问项
 * @author LV
 */
@Data
@Table("access")
public class Access {
    @Col(id=true, idSeq=0)
    public String proId;
    
    @Col(id=true, idSeq=1)
    @ColLike
    public String pattern;
    
    @ColLike
    public String name;
    
    public String des;
}
