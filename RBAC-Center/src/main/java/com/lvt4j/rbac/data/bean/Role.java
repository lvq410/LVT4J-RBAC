package com.lvt4j.rbac.data.bean;

import java.io.Serializable;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

/**
 * 角色
 * @author lichenxi
 */
@Data
@Table("role")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    public String proId;
    public String id;
    public String name;
    public String des;
}
