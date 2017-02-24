package com.lvt4j.rbac.web.controller;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lombok.NonNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TPager;
import com.lvt4j.rbac.data.bean.Access;
import com.lvt4j.rbac.data.bean.Param;
import com.lvt4j.rbac.data.bean.Permission;
import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.data.bean.Role;
import com.lvt4j.rbac.data.bean.RoleAccess;
import com.lvt4j.rbac.data.bean.RolePermission;
import com.lvt4j.rbac.data.bean.User;
import com.lvt4j.rbac.data.bean.UserAccess;
import com.lvt4j.rbac.data.bean.UserParam;
import com.lvt4j.rbac.data.bean.UserPermission;
import com.lvt4j.rbac.data.bean.UserRole;
import com.lvt4j.rbac.service.Cache;
import com.lvt4j.spring.JsonResult;


/**
 * 编辑用接口
 * @author lichenxi
 */
@RestController
@RequestMapping("/edit")
public class EditController {

    @Autowired
    TDB db;
    
    @Autowired
    Cache cache;
    
    @RequestMapping("/product/list")
    public JsonResult productList(
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager) {
        StringBuilder sql = new StringBuilder("select * from product");
        if(StringUtils.isNotEmpty(keyword)) {
            keyword = '%'+keyword+'%';
            sql.append(" where id like ? or name like ?");
            sql.append(" limit ?,?");
            return JsonResult.success(db.select(sql.toString(),
                    keyword, keyword, pager.getStart(), pager.getSize())
                    .execute2Model(Product.class));
        }
        sql.append(" limit ?,?");
        return JsonResult.success(db.select(sql.toString(),
                pager.getStart(), pager.getSize())
                .execute2Model(Product.class));
    }
    @RequestMapping("/product/set")
    public JsonResult productSet(
            @RequestParam(required=false) String oldId,
            Product product) {
        db.beginTransaction();
        if(oldId==null) {
            if(db.select("select count(id)<>0 from product where id=?",
                    product.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+product.id+"]的产品已存在，请另取ID!");
            product.lastModify = System.currentTimeMillis();
            db.insert(product).execute();
            productNotify(product.id);
        } else {
            if(!db.select("select count(id)<>0 from product where id=?",
                    oldId).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+oldId+"]的产品不存在,请刷新页面或重新查询!");
            if(!oldId.equals(product.id)
                    && db.select("select count(id)<>0 from product where id=?",
                            product.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+product.id+"]的产品已存在，请另取ID!");
            product.lastModify = System.currentTimeMillis();
            db.executeSQL("update product set id=?,name=?,des=?,lastModify=?,adminUserId=? where id=?",
                    product.id, product.name, product.des, product.lastModify, product.adminUserId, oldId)
                    .execute();
            if(oldId!=product.id) productNotify(oldId);
            productNotify(product.id);
        }
        return JsonResult.success();
    }
    @RequestMapping("/product/del")
    public JsonResult productDel(
            @RequestParam String id) {
        db.beginTransaction();
        if(!db.select("select count(id)<>0 from product where id=?", id)
                .execute2BasicOne(boolean.class))
            return JsonResult.fail("不存在产品[id="+id+"],请刷新页面或重新查询!");
        db.executeSQL("delete from product where id=?", id).execute();
        productNotify(id);
        return JsonResult.success();
    }
    
    @RequestMapping("/user/list")
    public JsonResult userList(
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager) {
        StringBuilder sql = new StringBuilder("select * from user");
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append(" where id like ? or name like ?");
            sql.append(" limit ?,?");
            return JsonResult.success(db.select(sql.toString(),
                    keyword, keyword, pager.getStart(), pager.getSize())
                    .execute2Model(User.class));
        }
        sql.append(" limit ?,?");
        return JsonResult.success(db.select(sql.toString(),
                pager.getStart(), pager.getSize())
                .execute2Model(User.class));
    }
    @RequestMapping("/user/set")
    public JsonResult userSet(
            @RequestParam(required=false) String oldId,
            User user){
        db.beginTransaction();
        // TODO 统计用户涉及到的产品并发送更改通知
        if(oldId==null) {
            if(db.select("select count(id)<>0 from user where id=?",
                    user.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+user.id+"]的用户已存在，请另取ID!");
            db.insert(user).execute();
            // TODO 
        } else {
            if(!db.select("select count(id)<>0 from user where id=?",
                    oldId).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+oldId+"]的用户不存在,请刷新页面或重新查询!");
            if(!oldId.equals(user.id)
                    && db.select("select count(id)<>0 from user where id=?",
                            user.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+user.id+"]的用户已存在，请另取ID!");
            db.executeSQL("update user set id=?,name=?,des=? where id=?",
                    user.id, user.name, user.des, oldId).execute();
            // TODO 
        }
        return JsonResult.success();
    }
    @RequestMapping("/user/del")
    public JsonResult userDel(
            @RequestParam String id){
        db.beginTransaction();
        if(!db.select("select count(id)<>0 from user where id=?", id)
                .execute2BasicOne(boolean.class)) 
            return JsonResult.fail("不存在用户[id="+id+"],请刷新页面或重新查询!");
        db.executeSQL("delete from user where id=?", id).execute();
        // TODO 统计用户涉及到的产品并发送更改通知
        productNotify();
        return JsonResult.success();
    }
    @RequestMapping("/user/getAuth")
    public JsonResult userGetAuth(
            @RequestParam String proId,
            @RequestParam String id){
        User user = db.select("select * from user where id=?",
                id).execute2ModelOne(User.class);
        
        List<Param> params = db.select("select * from param where proId=?",
                proId).execute2Model(Param.class);
        Map<String, String> userParams = new TreeMap<String, String>();
        List<UserParam> rawUserParams = db.select("select key,val from user_param where proId=? and userId=?",
                proId, id).execute2Model(UserParam.class);
        for(UserParam rawUserParam : rawUserParams) userParams.put(rawUserParam.key, rawUserParam.val);
        
        List<Role> userRoles = new LinkedList<Role>();
        List<String> userRoleIds = db.select("select roleId from user_role where proId=? and userId=?",
                proId, id).execute2Basic(String.class);
        for(String userRoleId : userRoleIds) {
            Role userRole = db.select("select * from role where proId=? and id=?",
                    proId, userRoleId).execute2ModelOne(Role.class);
            if(userRole==null)
                return JsonResult.fail("产品["+proId+"]下用户["+id+"]的ID为["
                        +userRoleId+"]的角色不存在,请刷新页面或重新查询!");
            userRoles.add(userRole);
        }
        
        List<Access> userAccesses = new LinkedList<Access>();
        List<String> userAccessPatterns = db.select("select accessPattern from user_access where proId=? and userId=?",
                proId, id).execute2Basic(String.class);
        for(String userAccessPattern : userAccessPatterns) {
            Access userAccess = db.select("select * from access where pattern=?",
                    userAccessPattern).execute2ModelOne(Access.class);
            if(userAccess==null)
                return JsonResult.fail("产品["+proId+"]下用户["+id+"]的pattern为["
                        +userAccessPattern+"]的访问项不存在,请刷新页面或重新查询!");
            userAccesses.add(userAccess);
        }
        
        List<Permission> userPermissions = new LinkedList<Permission>();
        List<String> userPermissionIds = db.select("select permissionId from user_permission where proId=? and userId=?",
                proId, id).execute2Basic(String.class);
        for(String userPermissionId : userPermissionIds) {
            Permission userPermission = db.select("select * from permission where id=?",
                    userPermissionId).execute2ModelOne(Permission.class);
            if(userPermission==null)
                return JsonResult.fail("产品["+proId+"]下用户["+id+"]的ID为["
                        +userPermissionId+"]的授权项不存在,请刷新页面或重新查询!");
            userPermissions.add(userPermission);
        }
        return JsonResult.success()
                .dataPut("user", user)
                .dataPut("params", params)
                .dataPut("userParams", userParams)
                .dataPut("userRoles", userRoles)
                .dataPut("userAccesses", userAccesses)
                .dataPut("userPermissions", userPermissions)
                .dataPut("mergeAccesses", mergeAccesses(proId, userRoleIds, userAccessPatterns))
                .dataPut("mergePermissions", mergePermissions(proId, userRoleIds, userPermissionIds));
    }
    @RequestMapping("/user/calAuth")
    public JsonResult userCalAuth(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds) {
        List<String> userRoleIds = roleIds==null?
                new LinkedList<String>()
                :Arrays.asList(roleIds);
        List<String> userAccessPatterns = accessPatterns==null?
                new LinkedList<String>()
                :Arrays.asList(accessPatterns);
        List<String> userPermissionIds = permissionIds==null?
                new LinkedList<String>()
                :Arrays.asList(permissionIds);
        return JsonResult.success()
                .dataPut("mergeAccesses", mergeAccesses(proId, userRoleIds, userAccessPatterns))
                .dataPut("mergePermissions", mergePermissions(proId, userRoleIds, userPermissionIds));
    }
    
    @RequestMapping("/user/setAuth")
    public JsonResult userSetAuth(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionsIds){
        db.beginTransaction();
        //用户配置
        db.executeSQL("delete from user_param where proId=? and userId=?",
                proId, userId).execute();
        if(params!=null){
            UserParam userParam = new UserParam();
            userParam.proId = proId;
            userParam.userId = userId;
            for(Object key : params.keySet()){
                userParam.key = key.toString();
                userParam.val = params.getString(userParam.key);
                db.insert(userParam).execute();
            }
        }
        //角色修改
        db.executeSQL("delete from user_role where proId=? and userId=?",
                proId, userId).execute();
        if(roleIds!=null) {
            UserRole userRole = new UserRole();
            userRole.proId = proId;
            userRole.userId = userId;
            for(String roleId: roleIds){
                userRole.roleId = roleId;
                db.insert(userRole).execute();
            }
        }
        //访问项修改
        db.executeSQL("delete from user_access where proId=? and userId=?",
                proId, userId).execute();
        if(accessPatterns!=null) {
            UserAccess userAccess = new UserAccess();
            userAccess.proId = proId;
            userAccess.userId = userId;
            for(String accessPattern: accessPatterns){
                userAccess.accessPattern = accessPattern;
                db.insert(userAccess).execute();
            }
        }
        //授权项修改
        db.executeSQL("delete from user_permission where proId=? and userId=?",
                proId, userId).execute();
        if(permissionsIds!=null) {
            UserPermission userPermission = new UserPermission();
            userPermission.proId = proId;
            userPermission.userId = userId;
            for(String permissionId: permissionsIds){
                userPermission.permissionId = permissionId;
                db.insert(userPermission).execute();
            }
        }
        // TODO 产品变动通知
        productNotify(proId);
        return JsonResult.success();
    }
    
    private Set<Access> mergeAccesses(@NonNull String proId, @NonNull List<String> roleIds, @NonNull List<String> accessePatterns) {
        Set<Access> mergeAccesses = new HashSet<Access>();
        Set<String> mergeAccessePatterns = new HashSet<String>(accessePatterns);
        for(String roleId : roleIds)
            mergeAccessePatterns.addAll(
                    db.select("select accessPattern from role_access where proId=? and roleId=?",
                            proId, roleId).execute2Basic(String.class));
        for(String pattern : mergeAccessePatterns){
            Access access = db.select("select * from access where proId=? and pattern=?",
                    proId, pattern).execute2ModelOne(Access.class);
            if(access==null) continue;
            mergeAccesses.add(access);
        }
        return mergeAccesses;
    }
    private Set<Permission> mergePermissions(@NonNull String proId, @NonNull List<String> roleIds, @NonNull List<String> permissionIds) {
        Set<Permission> mergePermissions = new HashSet<Permission>();
        Set<String> mergePermissionIds = new HashSet<String>(permissionIds);
        for(String roleId : roleIds)
            mergePermissionIds.addAll(
                    db.select("select permissionId from role_permission where proId=? and roleId=?",
                            proId, roleId).execute2Basic(String.class));
        for(String permissionId : mergePermissionIds){
            Permission permission = db.select("select * from permission where proId=? and id=?",
                    proId, permissionId).execute2ModelOne(Permission.class);
            if(permission==null) continue;
            mergePermissions.add(permission);
        }
        return mergePermissions;
    }
    
    
    @RequestMapping("/curProSet")
    public JsonResult curProSet(
            @RequestParam String curProId) {
        return JsonResult.success();
    }
    
    @RequestMapping("/param/list")
    public JsonResult paramList(
            @RequestParam String proId){
        return JsonResult.success(db.select("select * from param where proId=?", proId)
                .execute2Model(Param.class));
    }
    @RequestMapping("/param/set")
    public JsonResult paramSet(
            @RequestParam(required=false) String oldKey,
            Param param){
        db.beginTransaction();
        if(oldKey==null) {
            if(db.select("select count(key)<>0 from param where proId=? and key=?",
                    param.proId, param.key).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+param.proId+"]下key为["
                    +param.key+"]的配置项已存在,请另取key!");
            db.insert(param).execute();
        } else {
            if(!db.select("select count(key)<>0 from param where proId=? and key=?",
                    param.proId, oldKey).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+param.proId+"]下key为["
                    +oldKey+"]的配置项不存在,请刷新页面或重新查询!");
            if(!oldKey.equals(param.key)
                    && db.select("select count(key)<>0 from param where proId=? and key=?",
                            param.proId, param.key).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+param.proId+"]下key为["
                            +param.key+"]的配置项已存在,请另取key!");
            db.executeSQL("update param set key=?, name=?, des=? where proId=? and key=?",
                    param.key, param.name, param.des, param.proId, oldKey).execute();
        }
        productNotify(param.proId);
        return JsonResult.success();
    }
    @RequestMapping("/param/del")
    public JsonResult paramDel(
            @RequestParam String proId,
            @RequestParam String key){
        db.beginTransaction();
        if(!db.select("select count(key)<>0 from param where proId=? and key=?",
                proId, key).execute2BasicOne(boolean.class))
            return JsonResult.fail("产品["+proId+"]下key为["
                +key+"]的配置项不存在,请刷新页面或重新查询!");
        db.executeSQL("delete from param where proId=? and key=?", proId, key).execute();
        productNotify(proId);
        return JsonResult.success();
    }
    
    @RequestMapping("/access/list")
    public JsonResult accessList(
            @RequestParam String proId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        StringBuilder sql = new StringBuilder("select * from access where proId=?");
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append(" and (pattern like ? or name like ?)");
            sql.append(" limit ?,?");
            return JsonResult.success(db.select(sql.toString(),
                    proId, keyword, keyword, pager.getStart(), pager.getSize())
                    .execute2Model(Access.class));
        }
        sql.append(" limit ?,?");
        return JsonResult.success(db.select(sql.toString(),
                proId, pager.getStart(), pager.getSize())
                .execute2Model(Access.class));
    }
    @RequestMapping("/access/set")
    public JsonResult accessSet(
            @RequestParam(required=false) String oldPattern,
            Access access){
        db.beginTransaction();
        if(oldPattern==null) {
            if(db.select("select count(pattern)<>0 from access where proId=? and pattern=?",
                    access.proId, access.pattern).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+access.proId+"]下pattern为["
                    +access.pattern+"]的访问项已存在,请另取pattern!");
            db.insert(access).execute();
        } else {
            if(!db.select("select count(pattern)<>0 from access where proId=? and pattern=?",
                    access.proId, oldPattern).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+access.proId+"]下pattern为["
                    +oldPattern+"]的访问项不存在,请刷新页面或重新查询!");
            if(!oldPattern.equals(access.pattern)
                    && db.select("select count(pattern)<>0 from access where proId=? and pattern=?",
                            access.proId, access.pattern).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+access.proId+"]下pattern为["
                            +access.pattern+"]的访问项已存在,请另取pattern!");
            db.executeSQL("update access set pattern=?, name=?, des=? where proId=? and pattern=?",
                    access.pattern, access.name, access.des, access.proId, oldPattern).execute();
        }
        productNotify(access.proId);
        return JsonResult.success();
    }
    @RequestMapping("/access/del")
    public JsonResult accessDel(
            @RequestParam String proId,
            @RequestParam String pattern){
        db.beginTransaction();
        if(!db.select("select count(pattern)<>0 from access where proId=? and pattern=?",
                proId, pattern).execute2BasicOne(boolean.class))
            return JsonResult.fail("产品["+proId+"]下pattern为["
                +pattern+"]的访问项不存在,请刷新页面或重新查询!");
        db.executeSQL("delete from access where proId=? and pattern=?", proId, pattern).execute();
        productNotify(proId);
        return JsonResult.success();
    }
    
    @RequestMapping("/permission/list")
    public JsonResult permissionList(
            @RequestParam String proId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        StringBuilder sql = new StringBuilder("select * from permission where proId=?");
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append(" and (id like ? or name like ?)");
            sql.append(" limit ?,?");
            return JsonResult.success(db.select(sql.toString(),
                    proId, keyword, keyword, pager.getStart(), pager.getSize())
                    .execute2Model(Permission.class));
        }
        sql.append(" limit ?,?");
        return JsonResult.success(db.select(sql.toString(),
                proId, pager.getStart(), pager.getSize())
                .execute2Model(Permission.class));
    }
    @RequestMapping("/permission/set")
    public JsonResult permissionSet(
            @RequestParam(required=false) String oldId,
            Permission permission){
//        db.beginTransaction();
        if(oldId==null) {
            if(db.select("select count(id)<>0 from permission where proId=? and id=?",
                    permission.proId, permission.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+permission.proId+"]下ID为["
                    +permission.id+"]的授权项已存在,请另取ID!");
            db.insert(permission).execute();
        } else {
            if(!db.select("select count(id)<>0 from permission where proId=? and id=?",
                    permission.proId, oldId).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+permission.proId+"]下ID为["
                    +oldId+"]的授权项不存在,请刷新页面或重新查询!");
            if(!oldId.equals(permission.id)
                    && db.select("select count(id)<>0 from permission where proId=? and id=?",
                            permission.proId, permission.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+permission.proId+"]下ID为["
                            +permission.id+"]的授权项已存在,请另取ID!");
            db.executeSQL("update permission set id=?, name=?, des=? where proId=? and id=?",
                    permission.id, permission.name, permission.des, permission.proId, oldId).execute();
        }
        productNotify(permission.proId);
        return JsonResult.success();
    }
    @RequestMapping("/permission/del")
    public JsonResult permissionDel(
            @RequestParam String proId,
            @RequestParam String id){
        db.beginTransaction();
        if(!db.select("select count(id)<>0 from permission where proId=? and id=?",
                proId, id).execute2BasicOne(boolean.class))
            return JsonResult.fail("产品["+proId+"]下ID为["
                +id+"]的授权项不存在,请刷新页面或重新查询!");
        db.executeSQL("delete from permission where proId=? and id=?", proId, id).execute();
        productNotify(proId);
        return JsonResult.success();
    }
    
    @RequestMapping("/role/list")
    public JsonResult roleList(
            @RequestParam String proId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        StringBuilder sql = new StringBuilder("select * from role where proId=?");
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append(" and (id like ? or name like ?)");
            sql.append(" limit ?,?");
            return JsonResult.success(db.select(sql.toString(),
                    proId, keyword, keyword, pager.getStart(), pager.getSize())
                    .execute2Model(Role.class));
        }
        sql.append(" limit ?,?");
        return JsonResult.success(db.select(sql.toString(),
                proId, pager.getStart(), pager.getSize())
                .execute2Model(Role.class));
    }
    @RequestMapping("/role/get")
    public JsonResult roleGet(
            @RequestParam String proId,
            @RequestParam String id) {
        Role role = db.select("select * from role where proId=? and id=?",
                proId, id).execute2ModelOne(Role.class);
        if(role==null)
            return JsonResult.fail("产品["+proId+"]下ID为["
                    +id+"]的角色不存在,请刷新页面或重新查询!");
        List<String> accessPatterns = db.select("select accessPattern from role_access where proId=? and roleId=?",
                proId, id).execute2Basic(String.class);
        List<Access> accesses = new LinkedList<Access>();
        for (String accessPattern : accessPatterns) {
            Access access = db.select("select * from access where proId=? and pattern=?",
                    proId, accessPattern).execute2ModelOne(Access.class);
            if(access==null) return JsonResult.fail("产品["+proId+"]下角色["+id+"]的pattern为["
                    +accessPattern+"]的访问项不存在,请刷新页面或重新查询!");
            accesses.add(access);
        }
        List<String> permissionIds = db.select("select permissionId from role_permission where proId=? and roleId=?",
                proId, id).execute2Basic(String.class);
        List<Permission> permissions = new LinkedList<Permission>();
        for (String permissionId : permissionIds) {
            Permission permission = db.select("select * from permission where proId=? and id=?",
                    proId, permissionId).execute2ModelOne(Permission.class);
            if(permission==null) return JsonResult.fail("产品["+proId+"]下角色["+id+"]的ID为["
                    +permissionId+"]的授权项不存在,请刷新页面或重新查询!");
            permissions.add(permission);
        }
        return JsonResult.success()
                .dataPut("role", role)
                .dataPut("accesses", accesses)
                .dataPut("permissions", permissions);
    }
    @RequestMapping("/role/set")
    public JsonResult roleSet(
            @RequestParam(required=false) String oldId,
            Role role,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds){
        db.beginTransaction();
        if(oldId==null) {
            if(db.select("select count(id)<>0 from role where proId=? and id=?",
                role.proId, role.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+role.proId+"]下ID为["+role.id+"]的角色已存在,请另取ID!");
            db.insert(role).execute();
        } else {
            if(!db.select("select count(id)<>0 from role where proId=? and id=?",
                role.proId, oldId).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+role.proId+"]下ID为["+oldId+"]的角色不存在,请刷新页面或重新查询!");
            if(!oldId.equals(role.id)
                    && db.select("select count(id)<>0 from role where proId=? and id=?",
                            role.proId, role.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("产品["+role.proId+"]下ID为["+role.id+"]的角色已存在,请另取ID!");
            db.executeSQL("update role set id=?, name=?, des=? where proId=? and id=?",
                    role.id, role.name, role.des, role.proId, oldId).execute();
        }
        //访问项修改
        db.executeSQL("delete from role_access where proId=? and roleId=?", role.proId, role.id).execute();
        if(accessPatterns!=null) {
            RoleAccess roleAccess = new RoleAccess();
            roleAccess.proId = role.proId;
            roleAccess.roleId = role.id;
            for(String accessPattern: accessPatterns){
                roleAccess.accessPattern = accessPattern;
                db.insert(roleAccess).execute();
            }
        }
        //授权项修改
        db.executeSQL("delete from role_permission where proId=? and roleId=?", role.proId, role.id).execute();
        if(permissionIds!=null) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.proId = role.proId;
            rolePermission.roleId = role.id;
            for(String permissionId: permissionIds){
                rolePermission.permissionId = permissionId;
                db.insert(rolePermission).execute();
            }
        }
        productNotify(role.proId);
        return JsonResult.success();
    }
    @RequestMapping("/role/del")
    public JsonResult roleDel(
            @RequestParam String proId,
            @RequestParam String id){
        if(!db.select("select count(id)<>0 from role where proId=? and id=?",
                proId, id).execute2BasicOne(boolean.class))
            return JsonResult.fail("产品["+proId+"]下ID为["+id+"]的角色不存在,请刷新页面或重新查询!");
        db.executeSQL("delete from role where proId=? and id=?", proId, id).execute();
        productNotify(proId);
        return JsonResult.success();
    }
    
    /** 产品变动通知 */
    private void productNotify(String... proIds) {
        //TODO
    }
}
