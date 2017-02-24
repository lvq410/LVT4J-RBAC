package com.lvt4j.rbac.data.bean;

import java.io.Serializable;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

/**
 * 授权项
 * @author lichenxi
 */
@Data
@Table("permission")
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    public String proId;
    public String id;
    public String name;
    public String des;
}
