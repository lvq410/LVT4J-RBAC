package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Table;

@Data
@Table("role_permission")
public class RolePermission{

    public String proId;
    public String roleId;
    public String permissionId;
    
}
