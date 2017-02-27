package com.lvt4j.rbac.web.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import net.sf.json.JSONObject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.lvt4j.rbac.data.bean.VisitorAccess;
import com.lvt4j.rbac.data.bean.VisitorParam;
import com.lvt4j.rbac.data.bean.VisitorPermission;
import com.lvt4j.rbac.data.bean.VisitorRole;
import com.lvt4j.rbac.data.bean.base.BaseParam;
import com.lvt4j.rbac.service.ProductAuthCache;
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
    ProductAuthCache productAuthCache;
    
    @RequestMapping("/curProSet")
    public JsonResult curProSet(
            @RequestParam String curProId) {
        return JsonResult.success();
    }
    
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
            db.executeSQL("update product set id=?,name=?,des=?,lastModify=? where id=?",
                    product.id, product.name, product.des, product.lastModify, oldId)
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
    @RequestMapping("/user/get")
    public JsonResult userGet(
            @RequestParam String id) {
        return JsonResult.success(db.select("select * from user where id=?", id)
                .execute2ModelOne(User.class));
    }
    @RequestMapping("/user/set")
    public JsonResult userSet(
            @RequestParam(required=false) String oldId,
            User user){
        db.beginTransaction();
        if(oldId==null) {
            if(db.select("select count(id)<>0 from user where id=?",
                    user.id).execute2BasicOne(boolean.class))
                return JsonResult.fail("ID为["+user.id+"]的用户已存在，请另取ID!");
            db.insert(user).execute();
            productNotify(userProducts(user.id));
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
            productNotify(userProducts(user.id));
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
        productNotify(userProducts(id));
        return JsonResult.success();
    }
    private String[] userProducts(String userId) {
        List<String> list = db.select(
                "select distinct proId from ("
                    +"select proId from user_param where userId=?"
                    +" union select proId from user_role where userId=?"
                    +" union select proId from user_access where userId=?"
                    +" union select proId from user_permission where userId=?"
                +")", userId, userId, userId, userId).execute2Basic(String.class);
        return list.toArray(new String[list.size()]);
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
        List<Role> roles = null;
        StringBuilder sql = new StringBuilder("select * from role where proId=?");
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append(" and (id like ? or name like ?)");
            sql.append(" limit ?,?");
            roles = db.select(sql.toString(),
                    proId, keyword, keyword, pager.getStart(), pager.getSize())
                    .execute2Model(Role.class);
        } else {
            sql.append(" limit ?,?");
            roles = db.select(sql.toString(),
                    proId, pager.getStart(), pager.getSize())
                    .execute2Model(Role.class);
        }
        for(Role role : roles){
            Map<String, Object> auth = new HashMap<String, Object>();
            auth.put("accesses", db.select(
                    "select * from access where proId=? and pattern in("
                        +"select accessPattern from role_access where proId=? and roleId=?"
                    +")", proId, proId, role.id).execute2Model(Access.class));
            auth.put("permissions", db.select(
                    "select * from permission where proId=? and id in("
                        +"select permissionId from role_permission where proId=? and roleId=?"
                    +")", proId, proId, role.id).execute2Model(Permission.class));
            role.auth = auth;
        }
        return JsonResult.success(roles);
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
    
    @RequestMapping("/auth/visitor/get")
    public JsonResult authVisitorGet(
            @RequestParam String proId){
        List<Param> params = db.select("select * from param where proId=?",
                proId).execute2Model(Param.class);
        Map<String, String> visitorParams = BaseParam.toMap(db.select(
                "select key,val from visitor_param where proId=?",
                    proId).execute2Model(BaseParam.class));
        List<Role> visitorRoles = db.select("select * from role where id in("
                +"select roleId from visitor_role where proId=?)",
                    proId).execute2Model(Role.class);
        List<Access> visitorAccesses = db.select("select * from access where pattern in("
                +"select accessPattern from visitor_access where proId=?)",
                    proId).execute2Model(Access.class);
        List<Permission> visitorPermissions = db.select("select * from permission where id in("
                +"select permissionId from visitor_permission where proId=?)",
                    proId).execute2Model(Permission.class);
        List<Access> allAccesses = db.select(
                "select * from access where proId=? and pattern in("
                    +"select distinct accessPattern from (" //所有访问项去重
                        +"select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                            +"select roleId from visitor_role where proId=?" //游客拥有的角色
                        +") "
                        +"union "
                        +"select accessPattern from visitor_access where proId=?" //联合分配给游客的访问项
                    +")"
                +")", proId, proId, proId, proId).execute2Model(Access.class);
        List<Permission> allPermissions = db.select(
                "select * from permission where proId=? and id in("
                    +"select distinct permissionId from (" //所有授权项去重
                        +"select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                            +"select roleId from visitor_role where proId=?" //游客拥有的角色
                        +")"
                        +"union "
                        +"select permissionId from visitor_permission where proId=?" //联合分配给游客的授权项
                    +")"
                +")", proId, proId, proId, proId).execute2Model(Permission.class);
        return JsonResult.success()
                .dataPut("params", params)
                .dataPut("visitorParams", visitorParams)
                .dataPut("visitorRoles", visitorRoles)
                .dataPut("visitorAccesses", visitorAccesses)
                .dataPut("visitorPermissions", visitorPermissions)
                .dataPut("allAccesses", allAccesses)
                .dataPut("allPermissions", allPermissions);
    }
    @RequestMapping("/auth/visitor/set")
    public JsonResult authVisitorSet(
            @RequestParam String proId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds){
        db.beginTransaction();
        //游客配置配置项
        db.executeSQL("delete from visitor_param where proId=?", proId).execute();
        if(MapUtils.isNotEmpty(params)){
            VisitorParam visitorParam = new VisitorParam();
            visitorParam.proId = proId;
            for(Object key : params.keySet()){
                visitorParam.key = key.toString();
                visitorParam.val = params.getString(visitorParam.key);
                db.insert(visitorParam).execute();
            }
        }
        //角色修改
        db.executeSQL("delete from visitor_role where proId=?", proId).execute();
        if(ArrayUtils.isNotEmpty(roleIds)) {
            VisitorRole visitorRole = new VisitorRole();
            visitorRole.proId = proId;
            for(String roleId: roleIds){
                visitorRole.roleId = roleId;
                db.insert(visitorRole).execute();
            }
        }
        //访问项修改
        db.executeSQL("delete from visitor_access where proId=?", proId).execute();
        if(ArrayUtils.isNotEmpty(permissionIds)) {
            VisitorAccess visitorAccess = new VisitorAccess();
            visitorAccess.proId = proId;
            for(String accessPattern: accessPatterns){
                visitorAccess.accessPattern = accessPattern;
                db.insert(visitorAccess).execute();
            }
        }
        //授权项修改
        db.executeSQL("delete from visitor_permission where proId=?", proId).execute();
        if(ArrayUtils.isNotEmpty(permissionIds)) {
            VisitorPermission visitorPermission = new VisitorPermission();
            visitorPermission.proId = proId;
            for(String permissionId: permissionIds){
                visitorPermission.permissionId = permissionId;
                db.insert(visitorPermission).execute();
            }
        }
        productNotify(proId);
        return JsonResult.success();
    }
    @RequestMapping("/auth/visitor/cal")
    public JsonResult authVisitorCal(
            @RequestParam String proId,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds) {
        StringBuilder allRoleSql = new StringBuilder();
        List<Object> allRoleArgs = new LinkedList<Object>();
        if(ArrayUtils.isNotEmpty(roleIds)){
            for(int i=0; i<roleIds.length; i++){
                if(i!=0) allRoleSql.append(',');
                allRoleSql.append("?");
                allRoleArgs.add(roleIds[i]);
            }
        }
        
        StringBuilder sql = new StringBuilder();
        List<Object> args = new LinkedList<Object>();
        
        sql.append("select * from access where proId=? and pattern in(")
            .append("select distinct accessPattern from (")
                .append("select accessPattern from role_access where proId=? and roleId in(")
                    .append(allRoleSql)
                .append(')');
        args.add(proId);
        args.add(proId);
        args.addAll(allRoleArgs);
        if(ArrayUtils.isNotEmpty(accessPatterns)){
            for(String accessPattern : accessPatterns){
                sql.append("union select ? ");
                args.add(accessPattern);
            }
        }
        sql.append("))");
        List<Access> allAccesses = db.select(sql.toString(),
                args.toArray(new Object[args.size()])).execute2Model(Access.class);
        
        sql.setLength(0);
        args.clear();
        
        sql.append("select * from permission where proId=? and id in(")
            .append("select distinct permissionId from (")
                .append("select permissionId from role_permission where proId=? and roleId in(")
                    .append(allRoleSql)
                .append(')');
        args.add(proId);
        args.add(proId);
        args.addAll(allRoleArgs);
        if(ArrayUtils.isNotEmpty(permissionIds)){
            for(String permissionId : permissionIds){
                sql.append("union select ? ");
                args.add(permissionId);
            }
        }
        sql.append("))");
        List<Permission> allPermissions = db.select(sql.toString(),
                args.toArray(new Object[args.size()])).execute2Model(Permission.class);
        
        return JsonResult.success()
                .dataPut("allAccesses", allAccesses)
                .dataPut("allPermissions", allPermissions);
    }
    
    @RequestMapping("/auth/user/list")
    public JsonResult authUserList(
            @RequestParam String proId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager) {
        @SuppressWarnings("unchecked")
        List<User> users = (List<User>)userList(keyword, pager).data();
        for(User user : users) user.auth = authUserGet(proId, user.id);
        return JsonResult.success(users);
    }
    private Map<String, Object> authUserGet(String proId, String id){
        List<Param> params = db.select("select * from param where proId=?",
                proId).execute2Model(Param.class);
        Map<String, String> userParams = BaseParam.toMap(db.select(
                "select key,val from user_param where proId=? and userId=?",
                    proId, id).execute2Model(BaseParam.class));
        List<Role> userRoles = db.select("select * from role where proId=? and id in("+
                "select roleId from user_role where proId=? and userId=?)",
                    proId, proId, id).execute2Model(Role.class);
        List<Access> userAccesses = db.select("select * from access where proId=? and pattern in("
                +"select accessPattern from user_access where proId=? and userId=?)",
                    proId, proId, id).execute2Model(Access.class);
        List<Permission> userPermissions = db.select("select * from permission where proId=? and id in("
                    +"select permissionId from user_permission where proId=? and userId=?)",
                        proId, proId, id).execute2Model(Permission.class);
        List<Role> allRoles = db.select("select * from role where proId=? and id in("
                +"select distinct role from ("
                    + "select roleId from user_role where proId=? and userId=? "
                    + "union "
                    + "select roleId from visitor_role where proId=?))",
                proId, proId, id, proId).execute2Model(Role.class);
        List<Access> allAccesses = db.select(
                "select * from access where proId=? and pattern in("
                    +"select distinct accessPattern from (" //所有访问项去重
                        +"select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                            +"select distinct roleId from (" //所有角色去重
                                +"select roleId from user_role where proId=? and userId=? " //用户拥有的角色
                                + "union "
                                + "select roleId from visitor_role where proId=?" //联合游客拥有的角色
                            +")"
                        +") "
                        +"union "
                        +"select accessPattern from user_access where proId=? and userId=?" //联合单独分配给用户的访问项
                        +"union "
                        +"select accessPattern from visitor_access where proId=?" // 联合分配给游客的访问项
                    +")"
                +")", proId, proId, proId, id, proId, proId, id, proId).execute2Model(Access.class);
        List<Permission> allPermissions = db.select(
                "select * from permission where proId=? and id in("
                    +"select distinct permissionId from (" //所有授权项去重
                        +"select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                            +"select distinct roleId from (" //
                                +"select roleId from user_role where proId=? and userId=? " //用户拥有的角色
                                +"union "
                                +"select roleId from visitor_role where proId=?" //联合游客拥有的角色
                            +")"
                        +")"
                        +"union "
                        +"select permissionId from user_permission where proId=? and userId=?" //联合单独分配给用户的授权项
                        +"union "
                        +"select permissionId from visitor_permission where proId=?" //联合分配给游客的授权项
                    +")"
                +")", proId, proId, proId, id, proId, proId, id, proId).execute2Model(Permission.class);
        Map<String, Object> auth = new HashMap<String, Object>();
        auth.put("params", params);
        auth.put("userParams", userParams);
        auth.put("userRoles", userRoles);
        auth.put("userAccesses", userAccesses);
        auth.put("userPermissions", userPermissions);
        auth.put("allRoles", allRoles);
        auth.put("allAccesses", allAccesses);
        auth.put("allPermissions", allPermissions);
        return auth;
    }
    @RequestMapping("/auth/user/set")
    public JsonResult authUserSet(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds){
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
        if(permissionIds!=null) {
            UserPermission userPermission = new UserPermission();
            userPermission.proId = proId;
            userPermission.userId = userId;
            for(String permissionId: permissionIds){
                userPermission.permissionId = permissionId;
                db.insert(userPermission).execute();
            }
        }
        productNotify(proId);
        return JsonResult.success();
    }
    @RequestMapping("/auth/user/cal")
    public JsonResult authUserCal(
            @RequestParam String proId,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds) {
        StringBuilder allRoleSql = new StringBuilder("select distinct roleId from (");
        List<Object> allRoleArgs = new LinkedList<Object>();
        if(ArrayUtils.isNotEmpty(roleIds)){
            for(int i=0; i<roleIds.length; i++){
                if(i!=0) allRoleSql.append("union ");
                allRoleSql.append("select ? as 'roleId' ");
                allRoleArgs.add(roleIds[i]);
            }
            allRoleSql.append("union ");
        }
        allRoleSql.append("select roleId from visitor_role where proId=?)");
        allRoleArgs.add(proId);
        
        StringBuilder sql = new StringBuilder();
        List<Object> args = new LinkedList<Object>();
        
        sql.append("select * from role where id in(")
            .append(allRoleSql)
            .append(')');
        args.addAll(allRoleArgs);
        List<Role> allRoles = db.select(sql.toString(),
                args.toArray(new Object[args.size()])).execute2Model(Role.class);
        
        sql.setLength(0);
        args.clear();
        
        sql.append("select * from access where proId=? and pattern in(")
            .append("select distinct accessPattern from (")
                .append("select accessPattern from role_access where proId=? and roleId in(")
                    .append(allRoleSql)
                .append(')');
        args.add(proId);
        args.add(proId);
        args.addAll(allRoleArgs);
        if(ArrayUtils.isNotEmpty(accessPatterns)){
            for(String accessPattern : accessPatterns){
                sql.append("union select ? as 'accessPattern' ");
                args.add(accessPattern);
            }
        }
        sql.append("union select accessPattern from visitor_access where proId=?))");
        args.add(proId);
        List<Access> allAccesses = db.select(sql.toString(),
                args.toArray(new Object[args.size()])).execute2Model(Access.class);
        
        sql.setLength(0);
        args.clear();
        
        sql.append("select * from permission where proId=? and id in(")
            .append("select distinct permissionId from (")
                .append("select permissionId from role_permission where proId=? and roleId in(")
                    .append(allRoleSql)
                .append(')');
        args.add(proId);
        args.add(proId);
        args.addAll(allRoleArgs);
        if(ArrayUtils.isNotEmpty(permissionIds)){
            for(String permissionId : permissionIds){
                sql.append("union select ? as 'permissionId' ");
                args.add(permissionId);
            }
        }
        sql.append("union select permissionId from visitor_permission where proId=?))");
        args.add(proId);
        List<Permission> allPermissions = db.select(sql.toString(),
                args.toArray(new Object[args.size()])).execute2Model(Permission.class);
        
        return JsonResult.success()
                .dataPut("allRoles", allRoles)
                .dataPut("allAccesses", allAccesses)
                .dataPut("allPermissions", allPermissions);
    }
    
    private void productNotify(@NonNull String... proIds) {
        for(String proId : proIds)
            productAuthCache.invalidate(proId);
    }
}
