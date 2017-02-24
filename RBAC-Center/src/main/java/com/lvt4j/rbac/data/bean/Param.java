package com.lvt4j.rbac.data.bean;

import java.io.Serializable;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

/**
 * 配置项
 * @author lichenxi
 */
@Data
@Table("param")
public class Param implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    public String proId;
    public String key;
    public String name;
    public String des;
}
