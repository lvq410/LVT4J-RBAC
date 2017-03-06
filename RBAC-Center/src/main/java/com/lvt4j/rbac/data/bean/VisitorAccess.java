package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;

import lombok.Data;

@Data
@Table("visitor_access")
public class VisitorAccess{
    
    @Col(id=true, idSeq=0)
    public int proAId;
    
    @Col(id=true, idSeq=1)
    public int accessAId;
    
    public int seq;
}
