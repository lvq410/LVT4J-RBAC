package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

@Data
@Table("visitor_access")
public class VisitorAccess{

    public String proId;
    public String accessPattern;
    
}
