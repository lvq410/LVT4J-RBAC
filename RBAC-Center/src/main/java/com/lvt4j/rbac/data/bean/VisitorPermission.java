package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;

@Data
@Table("visitor_permission")
public class VisitorPermission{
    
    @Col(id=true, idSeq=0)
    public int proAId;
    
    @Col(id=true, idSeq=1)
    public int permissionAId;
    
    public int seq;
}
