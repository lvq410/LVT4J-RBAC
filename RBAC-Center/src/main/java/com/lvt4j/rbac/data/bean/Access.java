package com.lvt4j.rbac.data.bean;

import java.io.Serializable;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

/**
 * 访问项
 * @author lichenxi
 */
@Data
@Table("access")
public class Access implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    public String proId;
    public String pattern;
    public String name;
    public String des;
}
