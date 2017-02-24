package com.lvt4j.rbac.data.bean;

import java.io.Serializable;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

/**
 * 产品
 * @author lichenxi
 */
@Data
@Table("product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String id;
    public String name;
    public String des;
    public long lastModify;
    public String adminUserId;
}
