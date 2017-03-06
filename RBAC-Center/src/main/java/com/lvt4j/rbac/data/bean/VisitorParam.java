package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;

import lombok.Data;

@Data
@Table("visitor_param")
public class VisitorParam{
    
    @Col(id=true, idSeq=1)
    public int paramAId;
    
    public String val;
}
