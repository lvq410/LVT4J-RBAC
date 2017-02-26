package com.lvt4j.rbac.data.bean;

import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.bean.base.BaseParam;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Table("user_permission")
public class UserPermission extends BaseParam{

    public String proId;
    public String userId;
    public String permissionId;
    
}
