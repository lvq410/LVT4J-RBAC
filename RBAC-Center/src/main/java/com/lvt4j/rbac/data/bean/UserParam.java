package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;

import lombok.Data;

@Data
@Table("user_param")
public class UserParam{
    
    @Col(id=true, idSeq=0)
    public int userAId;
    
    @Col(id=true, idSeq=1)
    public int proAId;
    
    @Col(id=true, idSeq=2)
    public int paramAId;
    
    public String val;
}
