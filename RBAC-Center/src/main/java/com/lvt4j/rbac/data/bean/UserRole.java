package com.lvt4j.rbac.data.bean;

import lombok.Data;

import com.lvt4j.basic.TDB.Table;

@Data
@Table("user_role")
public class UserRole{

    public String proId;
    public String userId;
    public String roleId;
    
}
