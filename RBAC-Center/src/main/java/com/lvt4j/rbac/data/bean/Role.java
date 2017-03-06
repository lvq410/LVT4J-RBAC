package com.lvt4j.rbac.data.bean;

import java.util.List;

import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.rbac.data.base.ProCtrlModel;

/**
 * 角色
 * @author LV
 */
@Table("role")
public class Role extends ProCtrlModel {
    
    @Like
    public String id;
    
    @NotCol
    public List<Access> accesses;
    @NotCol
    public List<Permission> permissions;
}
