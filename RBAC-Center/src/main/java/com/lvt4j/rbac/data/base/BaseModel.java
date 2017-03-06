package com.lvt4j.rbac.data.base;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.rbac.data.Like;

public abstract class BaseModel extends SeqModel{
    
    @Col(id=true, autoId=true)
    public Integer aId;
    
    @Like
    public String name;
    
    public String des;
    
}
