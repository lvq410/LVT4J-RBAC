package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Table;

@Data
@Table("visitor_permission")
public class VisitorPermission{

    public String proId;
    public String permissionId;
    
}
