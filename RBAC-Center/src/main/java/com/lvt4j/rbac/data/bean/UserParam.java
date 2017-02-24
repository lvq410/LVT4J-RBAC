package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;

import lombok.Data;

@Data
@Table("user_param")
public class UserParam{

    public String proId;
    public String userId;
    public String key;
    public String val;
    
}
