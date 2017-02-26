package com.lvt4j.rbac.data.bean;

import java.io.Serializable;
import java.util.Map;

import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;

import lombok.Data;

/**
 * 用户
 * @author lichenxi
 */
@Data
@Table("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String id;
    public String name;
    public String des;
    
    @NotCol
    public Map<?, ?> auth;
    
}
