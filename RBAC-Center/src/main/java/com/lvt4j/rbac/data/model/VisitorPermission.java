package com.lvt4j.rbac.data.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.Model;

@Data
@EqualsAndHashCode(callSuper=true)
@Table("visitor_permission")
public class VisitorPermission extends Model{
    
    @Col(id=true, idSeq=0)
    public int proAutoId;
    
    @Col(id=true, idSeq=1)
    public int permissionAutoId;
    
    public int seq;
}
