package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

@Data
@Table("role_access")
public class RoleAccess{

    public String proId;
    public String roleId;
    public String accessPattern;
    
}
