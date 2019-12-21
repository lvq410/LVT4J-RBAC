package com.lvt4j.rbac;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvt4j.basic.TDB;
import com.lvt4j.rbac.data.model.Access;
import com.lvt4j.rbac.data.model.Param;
import com.lvt4j.rbac.data.model.Permission;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.data.model.Role;
import com.lvt4j.rbac.data.model.RoleAccess;
import com.lvt4j.rbac.data.model.RolePermission;
import com.lvt4j.rbac.data.model.User;
import com.lvt4j.rbac.data.model.UserAccess;
import com.lvt4j.rbac.data.model.UserParam;
import com.lvt4j.rbac.data.model.UserPermission;
import com.lvt4j.rbac.data.model.UserRole;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试用数据初始化
 * @author LV
 */
@Slf4j
@Component
public class TestDataIniter {
    
//    public static int ProN = 100;
//    public static int UserN = 100;
//    public static int paramsN = 10;
//    public static int accessN = 100;
//    public static int permissionN = 100;
//    public static int roleN = 10;
//    public static int roleAccessN = 20;
//    public static int rolePermissionN = 20;
//    public static int userParamN = 5;
//    public static int userRoleN = 5;
//    public static int userAccessN = 50;
//    public static int userPermissionN = 50;
    public static int Pro0AutoId;
    public static int ProN = 10;
    public static int UserN = 100;
    public static int paramsN = 10;
    public static int accessN = 10;
    public static int permissionN = 10;
    public static int roleN = 5;
    public static int roleAccessN = 5;
    public static int rolePermissionN = 5;
    public static int userParamN = 5;
    public static int userRoleN = 3;
    public static int userAccessN = 8;
    public static int userPermissionN = 8;
    
    
    @Autowired
    private TDB db;
    
    List<Product> pros = new LinkedList<>();
    List<User> users = new LinkedList<>();
    Map<Integer, List<Param>> proParams = new HashMap<>();
    Map<Integer, List<Access>> proAccesses = new HashMap<>();
    Map<Integer, List<Permission>> proPermissions = new HashMap<>();
    Map<Integer, List<Role>> proRoles = new HashMap<>();
    
    public void init() throws Exception {
        initPros(ProN);
        initUsers(UserN);
        initAuthModels(paramsN, accessN, permissionN, roleN, roleAccessN, rolePermissionN);
        initUserAuths(userParamN, userRoleN, userAccessN, userPermissionN);
        log.info("初始化完成");
    }
    
    /** 创建n个产品 */
    private void initPros(int n) throws Exception {
        log.info("初始化产品...");
        for(int i=0; i<n; i++){
            Product pro = new Product();
            pro.id = pro.name = pro.des = "pro"+i;
            pro.lastModify = System.currentTimeMillis();
            pro.seq = i;
            db.insert(pro).execute();
            pros.add(pro);
            if(i==0) Pro0AutoId=pro.autoId;
        }
    }
    /** 创建n个用户 */
    public void initUsers(int n) throws Exception {
        log.info("初始化用户...");
        for(int i=0; i<n; i++) {
            User user = new User();
            user.id = user.name = user.des = "user"+i;
            user.seq = i;
            users.add(user);
            db.insert(user).execute();
        }
    }
    
    /** 每个产品创建paramsN个配置项,accessN个访问项,permissionN个授权,roleN个角色(角色拥有前roleAccessN个访问项和rolePermissionN个授权项) */
    private void initAuthModels(int paramsN, int accessN, int permissionN,
            int roleN, int roleAccessN, int rolePermissionN) {
        log.info("初始化权限对象...");
        for(Product pro : pros) {
            List<Param> params = new LinkedList<>();
            for(int i=0; i<paramsN; i++){
                Param param = new Param();
                param.proAutoId = pro.autoId;
                param.key = param.name = param.des = "param"+i;
                param.seq = i;
                db.insert(param).execute();
                params.add(param);
            }
            proParams.put(pro.autoId, params);
            List<Access> accesses = new LinkedList<>();
            for(int i=0; i<accessN; i++){
                Access access = new Access();
                access.proAutoId = pro.autoId;
                access.pattern = access.des = access.name = "^/"+i+"$";
                access.seq = i;
                db.insert(access).execute();
                accesses.add(access);
            }
            proAccesses.put(pro.autoId, accesses);
            List<Permission> permissions = new LinkedList<>();
            for(int i=0; i<permissionN; i++){
                Permission permission = new Permission();
                permission.proAutoId = pro.autoId;
                permission.id = permission.name = permission.des ="permission"+i;
                permission.seq = i;
                db.insert(permission).execute();
                permissions.add(permission);
            }
            proPermissions.put(pro.autoId, permissions);
            List<Role> roles = new LinkedList<>();
            for(int i=0; i<roleN; i++){
                Role role = new Role();
                role.proAutoId = pro.autoId;
                role.id = role.name = role.des = "role"+i;
                role.seq = i;
                db.insert(role).execute();
                roles.add(role);
            }
            proRoles.put(pro.autoId, roles);
            roleAccessN = Math.min(roleAccessN, accessN);
            rolePermissionN = Math.min(rolePermissionN, permissionN);
            for(Role role : roles){
                int roleAccessNo = 0;
                for(Access access : accesses){
                    RoleAccess roleAccess = new RoleAccess();
                    roleAccess.proAutoId = pro.autoId;
                    roleAccess.roleAutoId = role.autoId;
                    roleAccess.accessAutoId = access.autoId;
                    roleAccess.seq = roleAccessNo;
                    db.insert(roleAccess).execute();
                    roleAccessNo += 1;
                    if(roleAccessNo>=roleAccessN) break;
                }
                int rolePermissionNo = 0;
                for(Permission permission : permissions){
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.proAutoId = pro.autoId;
                    rolePermission.roleAutoId = role.autoId;
                    rolePermission.permissionAutoId = permission.autoId;
                    rolePermission.seq = rolePermissionNo;
                    db.insert(rolePermission).execute();
                    rolePermissionNo += 1;
                    if(rolePermissionNo>=rolePermissionN) break;
                }
            }
        }
    }
    
    /** 为每个用户在每个产品设置userParamN个配置项、分配userRoleN个角色、userAccessN个访问项、userPermissionN个授权项 */
    private void initUserAuths(int userParamN, int userRoleN, int userAccessN, int userPermissionN) throws Exception {
        
        log.info("初始化用户权限...");
        for(User user : users){
            for(Product pro : pros){
                List<Param> params = proParams.get(pro.autoId);
                int userParamCount = Math.min(userParamN, params.size());
                int userParamNo = 0;
                for(Param param : params){
                    UserParam userParam = new UserParam();
                    userParam.proAutoId = pro.autoId;
                    userParam.userAutoId = user.autoId;
                    userParam.paramAutoId = param.autoId;
                    userParam.val = "param"+param.autoId;
                    db.insert(userParam).execute();
                    userParamNo += 1;
                    if(userParamNo>=userParamCount) break;
                }
                List<Role> roles = proRoles.get(pro.autoId);
                int userRoleCount = Math.min(userRoleN, roles.size());
                int userRoleNo = 0;
                for(Role role : roles){
                    UserRole userRole = new UserRole();
                    userRole.proAutoId = pro.autoId;
                    userRole.userAutoId = user.autoId;
                    userRole.roleAutoId = role.autoId;
                    userRole.seq = userRoleNo;
                    db.insert(userRole).execute();
                    userRoleNo += 1;
                    if(userRoleNo>=userRoleCount) break;
                }
                List<Access> accesses = proAccesses.get(pro.autoId);
                int userAccessCount = Math.min(userAccessN, accesses.size());
                int userAccessNo = 0;
                for(Access access : accesses){
                    UserAccess userAccess = new UserAccess();
                    userAccess.proAutoId = pro.autoId;
                    userAccess.userAutoId = user.autoId;
                    userAccess.accessAutoId = access.autoId;
                    userAccess.seq = userAccessNo;
                    db.insert(userAccess).execute();
                    userAccessNo += 1;
                    if(userAccessNo>=userAccessCount) break;
                }
                List<Permission> permissions = proPermissions.get(pro.autoId);
                int userPermissionCount = Math.min(userPermissionN, permissions.size());
                int userPermissionNo = 0;
                for(Permission permission : permissions){
                    UserPermission userPermission = new UserPermission();
                    userPermission.proAutoId = pro.autoId;
                    userPermission.userAutoId = user.autoId;
                    userPermission.permissionAutoId = permission.autoId;
                    userPermission.seq = userPermissionNo;
                    db.insert(userPermission).execute();
                    userPermissionNo += 1;
                    if(userPermissionNo>=userPermissionCount) break;
                }
            }
        }
    }
    
}