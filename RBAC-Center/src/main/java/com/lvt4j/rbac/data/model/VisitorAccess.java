package com.lvt4j.rbac.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Model;

@Data
@EqualsAndHashCode(callSuper=true)
@Table("visitor_access")
public class VisitorAccess extends Model{
    
    @Col(id=true, idSeq=0)
    public int proAutoId;
    
    @Col(id=true, idSeq=1)
    public int accessAutoId;
    
    public int seq;
}
