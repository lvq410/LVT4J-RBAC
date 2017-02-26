package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Table;

@Data
@Table("visitor_role")
public class VisitorRole{

    public String proId;
    public String roleId;
    
}
