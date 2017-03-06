package com.lvt4j.rbac.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.basic.TCollection.TAutoMap;
import com.lvt4j.basic.TCollection.TAutoMap.ValueBuilder;
import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TPager;
import com.lvt4j.basic.TReflect;
import com.lvt4j.rbac.ProductAuth4Center;
import com.lvt4j.rbac.data.BaseModel;
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
import com.lvt4j.rbac.service.ProductAuthCache;
import com.lvt4j.rbac.web.DBInterceptor.Transaction;
import com.lvt4j.spring.JsonResult;


/**
 * 编辑用接口
 * @author LV
 */
@RestController
@RequestMapping("/edit")
public class EditController {

    @Autowired
    TDB db;
    
    @Autowired
    Lock editLock;
    
    @Autowired
    ProductAuthCache productAuthCache;
    
    TAutoMap<Class<? extends BaseModel>, TAutoMap<Integer, BaseModel>> beanCache = new TAutoMap<Class<? extends BaseModel>, TAutoMap<Integer, BaseModel>>(
            new ValueBuilder<Class<? extends BaseModel>, TAutoMap<Integer, BaseModel>>(){
        private static final long serialVersionUID = 1L;
        @Override
        public TAutoMap<Integer, BaseModel> build(Class<? extends BaseModel> modelCls){
            return new TAutoMap<Integer, BaseModel>(new ValueBuilder<Integer, BaseModel>(){
                private static final long serialVersionUID = 1L;
                @Override
                public BaseModel build(Integer aId){
                    return db.get(modelCls, aId).execute();
                }
            });
        }
    });
    
    @RequestMapping("/curProSet")
    public JsonResult curProSet(
            HttpSession session,
            @RequestParam int proAId){
        session.setAttribute("curPro", beanCache.get(Product.class).get(proAId));
        return JsonResult.success();
    }
    
    @RequestMapping("/product/list")
    public JsonResult productList(
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        return JsonResult.success(list(Product.class, null, keyword, pager));
    }
    @RequestMapping("/product/set")
    @Transaction
    public JsonResult productSet(
            Product product)throws Exception{
        if(unique(product)) return JsonResult.fail("产品[" + product.id + "]已存在，请另取ID!");
        set(product);
        productNotify(product.aId);
        return JsonResult.success();
    }
    @RequestMapping("/product/del")
    @Transaction
    public JsonResult productDel(
            @RequestParam int aId){
        del(Product.class, aId);
        productNotify(aId);
        return JsonResult.success();
    }
    @RequestMapping("/product/sort")
    @Transaction
    public JsonResult productDel(
            @RequestParam int aId){
        del(Product.class, aId);
        productNotify(aId);
        return JsonResult.success();
    }
    
    
    @RequestMapping("/user/list")
    public JsonResult userList(
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        return JsonResult.success(list(User.class, null, keyword, pager));
    }
    @RequestMapping("/user/set")
    @Transaction
    public JsonResult userSet(
            User user)throws Exception{
        if(unique(user)) return JsonResult.fail("用户["+user.id+"]已存在，请另取ID!");
        set(user);
        return JsonResult.success();
    }
    @RequestMapping("/user/del")
    @Transaction
    public JsonResult userDel(
            @RequestParam int aId){
        Integer[] userProducts = userProducts(aId);
        del(User.class, aId);
        productNotify(userProducts);
        return JsonResult.success();
    }
    private Integer[] userProducts(int userAId){
        return db.select("select proAId from user_param where userAId=? "
                    +"union select proAId from user_role where userAId=? "
                    +"union select proAId from user_access where userAId=? "
                    +"union select proAId from user_permission where userAId=?",
                    userAId, userAId, userAId, userAId).execute2Basic(Integer.class)
                    .toArray(new Integer[]{});
    }
    
    @RequestMapping("/param/list")
    public JsonResult paramList(
            @RequestParam int proAId){
        return JsonResult.success(list(Param.class, proAId, null, null));
    }
    @RequestMapping("/param/set")
    @Transaction
    public JsonResult paramSet(
            Param param)throws Exception{
        if(unique(param)) return JsonResult.fail("配置项["+param.key+"]已存在,请另取key!");
        set(param);
        productNotify(param.proAId);
        return JsonResult.success();
    }
    @RequestMapping("/param/del")
    @Transaction
    public JsonResult paramDel(
            @RequestParam int proAId,
            @RequestParam int aId){
        del(Param.class, aId);
        productNotify(proAId);
        return JsonResult.success();
    }
    
    @RequestMapping("/access/list")
    public JsonResult accessList(
            @RequestParam int proAId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        return JsonResult.success(list(Access.class, proAId, keyword, pager));
    }
    @RequestMapping("/access/patternMatch")
    public JsonResult accessPatternMatch(
            @RequestParam String pattern,
            @RequestParam String uri){
        return JsonResult.success(uri.matches(pattern));
    }
    @RequestMapping("/access/set")
    @Transaction
    public JsonResult accessSet(
            Access access)throws Exception{
        if(unique(access)) return JsonResult.fail("访问项["+access.pattern+"]已存在,请另取pattern!");
        set(access);
        productNotify(access.proAId);
        return JsonResult.success();
    }
    @RequestMapping("/access/del")
    @Transaction
    public JsonResult accessDel(
            @RequestParam int proAId,
            @RequestParam int aId){
        del(Access.class, aId);
        productNotify(proAId);
        return JsonResult.success();
    }
    
    @RequestMapping("/permission/list")
    public JsonResult permissionList(
            @RequestParam int proAId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        return JsonResult.success(list(Permission.class, proAId, keyword, pager));
    }
    @RequestMapping("/permission/set")
    @Transaction
    public JsonResult permissionSet(
            Permission permission){
        db.beginTransaction();
        if(oldId==null){
            if(db.exist(permission).execute())
                return JsonResult.fail("产品["+permission.proId+"]下授权项["
                    +permission.id+"]已存在,请另取ID!");
            db.insert(permission).execute();
        } else {
            if(!db.exist(Permission.class, permission.proId, oldId).execute())
                return JsonResult.fail("产品["+permission.proId+"]下授权项["
                    +oldId+"]不存在,请刷新页面或重新查询!");
            if(!oldId.equals(permission.id)
                    && db.exist(permission).execute())
                return JsonResult.fail("产品["+permission.proId+"]下授权项["
                            +permission.id+"]已存在,请另取ID!");
            db.update(permission, permission.proId, oldId).execute();
        }
        productNotify(permission.proId);
        return JsonResult.success();
    }
    @RequestMapping("/permission/del")
    public JsonResult permissionDel(
            @RequestParam int proAId,
            @RequestParam String id){
        db.beginTransaction();
        if(!db.exist(Permission.class, proId, id).execute())
            return JsonResult.fail("产品["+proId+"]下授权项["
                +id+"]不存在,请刷新页面或重新查询!");
        db.delete(Permission.class, proId, id).execute();
        productNotify(proId);
        return JsonResult.success();
    }
    
    @RequestMapping("/role/list")
    public JsonResult roleList(
            @RequestParam int proAId,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String accessPattern,
            @RequestParam(required=false) String permissionId,
            @RequestParam(required=false) boolean ignoreAuth,
            @RequestParam TPager pager){
        StringBuilder sql = new StringBuilder("select * from role where proId=? ");
        List<Object> args = new LinkedList<Object>();
        args.add(proId);
        if(StringUtils.isNotEmpty(accessPattern)){
            sql.append("and id in (select roleId from role_access where proId=? and accessPattern=?)");
            args.add(proId);
            args.add(accessPattern);
        }
        if(StringUtils.isNotEmpty(permissionId)){
            sql.append("and id in (select roleId from role_permission where proId=? and permissionId=?)");
            args.add(proId);
            args.add(permissionId);
        }
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append("and (id like ? or name like ?)");
            args.add(keyword);
            args.add(keyword);
        }
        sql.append(" limit ?,?");
        args.add(pager.getStart());
        args.add(pager.getSize());
        List<Role> roles = db.select(sql.toString(), args.toArray()).execute2Model(Role.class);
        if(ignoreAuth) return JsonResult.success(roles);
        for(Role role : roles){
            role.accesses = db.select(
                    "select * from access where proId=? and pattern in("
                        +"select accessPattern from role_access where proId=? and roleId=?"
                    +")", proId, proId, role.id).execute2Model(Access.class);
            role.permissions = db.select(
                    "select * from permission where proId=? and id in("
                        +"select permissionId from role_permission where proId=? and roleId=?"
                    +")", proId, proId, role.id).execute2Model(Permission.class);
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
        if(oldId==null){
            if(db.exist(role).execute())
                return JsonResult.fail("产品["+role.proId+"]下角色["+role.id+"]已存在,请另取ID!");
            db.insert(role).execute();
        } else {
            if(!db.exist(Role.class, role.proId, oldId).execute())
                return JsonResult.fail("产品["+role.proId+"]下角色["+oldId+"]不存在,请刷新页面或重新查询!");
            if(!oldId.equals(role.id)
                    && db.exist(role).execute())
                return JsonResult.fail("产品["+role.proId+"]下角色["+role.id+"]已存在,请另取ID!");
            db.update(role, role.proId, oldId).execute();
        }
        //访问项修改
        db.executeSQL("delete from role_access where proId=? and roleId=?", role.proId, role.id).execute();
        if(accessPatterns!=null){
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
        if(permissionIds!=null){
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
            @RequestParam int proAId,
            @RequestParam String id){
        if(!db.exist(Role.class, proId, id).execute())
            return JsonResult.fail("产品["+proId+"]下角色["+id+"]不存在,请刷新页面或重新查询!");
        db.delete(Role.class, proId, id).execute();
        productNotify(proId);
        return JsonResult.success();
    }
    
    @RequestMapping("/auth/visitor/get")
    public JsonResult authVisitorGet(
            @RequestParam int proAId){
        List<Param> params = db.select("select P.*,VP.val from param P "
                + "left join visitor_param VP on P.key=VP.key "
                + "where P.proId=? and VP.proId=?",
                proId, proId).execute2Model(Param.class);
        List<Role> roles = db.select("select * from role where proId=? and id in("
                +"select roleId from visitor_role where proId=?)",
                    proId, proId).execute2Model(Role.class);
        List<Access> accesses = db.select("select * from access where proId=? and pattern in("
                +"select accessPattern from visitor_access where proId=?)",
                    proId, proId).execute2Model(Access.class);
        List<Permission> permissions = db.select("select * from permission where proId=? and id in("
                +"select permissionId from visitor_permission where proId=?)",
                    proId, proId).execute2Model(Permission.class);
        List<Access> allAccesses = db.select(
                "select * from access where proId=? and pattern in("
                    +"select accessPattern from role_access where proId=? and roleId in(" //所有角色拥有的访问项
                        +"select roleId from visitor_role where proId=?" //游客拥有的角色
                    +") "
                    +"union "
                    +"select accessPattern from visitor_access where proId=?" //联合分配给游客的访问项
                +")", proId, proId, proId, proId).execute2Model(Access.class);
        List<Permission> allPermissions = db.select(
                "select * from permission where proId=? and id in("
                    +"select permissionId from role_permission where proId=? and roleId in(" //所有角色拥有的授权项
                        +"select roleId from visitor_role where proId=?" //游客拥有的角色
                    +")"
                    +"union "
                    +"select permissionId from visitor_permission where proId=?" //联合分配给游客的授权项
                +")", proId, proId, proId, proId).execute2Model(Permission.class);
        return JsonResult.success()
                .dataPut("params", params)
                .dataPut("roles", roles)
                .dataPut("accesses", accesses)
                .dataPut("permissions", permissions)
                .dataPut("allAccesses", allAccesses)
                .dataPut("allPermissions", allPermissions);
    }
    @RequestMapping("/auth/visitor/set")
    public JsonResult authVisitorSet(
            @RequestParam int proAId,
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
        if(ArrayUtils.isNotEmpty(roleIds)){
            VisitorRole visitorRole = new VisitorRole();
            visitorRole.proId = proId;
            for(String roleId: roleIds){
                visitorRole.roleId = roleId;
                db.insert(visitorRole).execute();
            }
        }
        //访问项修改
        db.executeSQL("delete from visitor_access where proId=?", proId).execute();
        if(ArrayUtils.isNotEmpty(accessPatterns)){
            VisitorAccess visitorAccess = new VisitorAccess();
            visitorAccess.proId = proId;
            for(String accessPattern: accessPatterns){
                visitorAccess.accessPattern = accessPattern;
                db.insert(visitorAccess).execute();
            }
        }
        //授权项修改
        db.executeSQL("delete from visitor_permission where proId=?", proId).execute();
        if(ArrayUtils.isNotEmpty(permissionIds)){
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
            @RequestParam int proAId,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds){
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
        sql.append(')');
        List<Access> allAccesses = db.select(sql.toString(),
                args.toArray()).execute2Model(Access.class);
        
        sql.setLength(0);
        args.clear();
        
        sql.append("select * from permission where proId=? and id in(")
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
        sql.append(')');
        List<Permission> allPermissions = db.select(sql.toString(),
                args.toArray()).execute2Model(Permission.class);
        
        return JsonResult.success()
                .dataPut("allAccesses", allAccesses)
                .dataPut("allPermissions", allPermissions);
    }
    
    @RequestMapping("/auth/user/list")
    public JsonResult authUserList(
            @RequestParam int proAId,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String roleId,
            @RequestParam(required=false) String accessPattern,
            @RequestParam(required=false) String permissionId,
            @RequestParam TPager pager){
        StringBuilder sql = new StringBuilder("select * from user ");
        StringBuilder whereClause = new StringBuilder();
        List<Object> args = new LinkedList<Object>();
        if(StringUtils.isNotEmpty(roleId)){
            whereClause.append("and id in (select userId from user_role where proId=? and roleId=?)");
            args.add(proId);
            args.add(roleId);
        }
        if(StringUtils.isNotEmpty(accessPattern)){
            whereClause.append("and id in (select userId from user_access where proId=? and accessPattern=?)");
            args.add(proId);
            args.add(accessPattern);
        }
        if(StringUtils.isNotEmpty(permissionId)){
            whereClause.append("and id in (select userId from user_permission where proId=? and permissionId=?)");
            args.add(proId);
            args.add(permissionId);
        }
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            whereClause.append("and (id like ? or name like ?)");
            args.add(keyword);
            args.add(keyword);
        }
        if(whereClause.length()!=0){
            whereClause.replace(0, 3, "");
            whereClause.insert(0, "where");
        }
        sql.append(whereClause).append(" limit ?,?");
        args.add(pager.getStart());
        args.add(pager.getSize());
        List<User> users = db.select(sql.toString(), args.toArray()).execute2Model(User.class);
        for (User user : users){
            user.params = db.select("select P.*,UP.val "
                + "from param P left join user_param UP on P.proId=UP.proId and UP.userId=? and P.key=UP.key "
                + "where P.proId=?",
                user.id, proId).execute2Model(Param.class);
            user.roles = db.select("select * from role where proId=? and id in("+
                "select roleId from user_role where proId=? and userId=?)",
                    proId, proId, user.id).execute2Model(Role.class);
            user.accesses = db.select("select * from access where proId=? and pattern in("
                +"select accessPattern from user_access where proId=? and userId=?)",
                    proId, proId, user.id).execute2Model(Access.class);
            user.permissions = db.select("select * from permission where proId=? and id in("
                +"select permissionId from user_permission where proId=? and userId=?)",
                    proId, proId, user.id).execute2Model(Permission.class);
        }
        return JsonResult.success(users);
    }
    @RequestMapping("/auth/user/set")
    public JsonResult authUserSet(
            @RequestParam int proAId,
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
        if(roleIds!=null){
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
        if(accessPatterns!=null){
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
        if(permissionIds!=null){
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
            @RequestParam int proAId,
            @RequestParam(required=false) String[] roleIds,
            @RequestParam(required=false) String[] accessPatterns,
            @RequestParam(required=false) String[] permissionIds){
        StringBuilder allRoleSql = new StringBuilder();
        List<Object> allRoleArgs = new LinkedList<Object>();
        if(ArrayUtils.isNotEmpty(roleIds)){
            for(int i=0; i<roleIds.length; i++){
                if(i!=0) allRoleSql.append("union ");
                allRoleSql.append("select ? as 'roleId' ");
                allRoleArgs.add(roleIds[i]);
            }
            allRoleSql.append("union ");
        }
        allRoleSql.append("select roleId from visitor_role where proId=?");
        allRoleArgs.add(proId);
        
        StringBuilder sql = new StringBuilder();
        List<Object> args = new LinkedList<Object>();
        
        sql.append("select * from role where proId=? and id in(")
            .append(allRoleSql)
            .append(')');
        args.add(proId);
        args.addAll(allRoleArgs);
        List<Role> allRoles = db.select(sql.toString(),
                args.toArray()).execute2Model(Role.class);
        
        sql.setLength(0);
        args.clear();
        
        sql.append("select * from access where proId=? and pattern in(")
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
        sql.append("union select accessPattern from visitor_access where proId=?)");
        args.add(proId);
        List<Access> allAccesses = db.select(sql.toString(),
                args.toArray()).execute2Model(Access.class);
        
        sql.setLength(0);
        args.clear();
        
        sql.append("select * from permission where proId=? and id in(")
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
        sql.append("union select permissionId from visitor_permission where proId=?)");
        args.add(proId);
        List<Permission> allPermissions = db.select(sql.toString(),
                args.toArray()).execute2Model(Permission.class);
        
        return JsonResult.success()
                .dataPut("allRoles", allRoles)
                .dataPut("allAccesses", allAccesses)
                .dataPut("allPermissions", allPermissions);
    }
    
    private void productNotify(Integer... proAIds){
        if(proIds==null){
            db.executeSQL("update product set lastModify=?",
                    System.currentTimeMillis()).execute();
            productAuthCache.clear();
            return;
        }
        for(int proAId : proIds){
            db.executeSQL("update product set lastModify=? where id=?",
                    System.currentTimeMillis(), proId).execute();
            productAuthCache.invalidate(proId);
        }
    }
    
    private <E> List<E> list(Class<E> modelCls, Integer proAId, String keyword, TPager pager){
        StringBuilder sql = new StringBuilder("select * from ")
            .append(modelCls.getAnnotation(Table.class).value()).append(" where seq>0 ");
        List<Object> args = new LinkedList<Object>();
        if(proAId!=null){
            sql.append("and proAId=? ");
            args.add(proAId);
        }
        if(StringUtils.isNotEmpty(keyword)){
            keyword = '%'+keyword+'%';
            sql.append("and (");
            boolean first = true;
            for(Field field : BaseModel.likeFields(modelCls)){
                if(!first) sql.append("or ");
                sql.append(field.getName()).append(" like ? ");
                args.add(keyword);
                first = false;
            }
            sql.append(") ");
        }
        sql.append("order by seq ");
        if(pager!=null){
            sql.append("limit ?,?");
            args.add(pager.getStart());
            args.add(pager.getSize());
        }
        return db.select(sql.toString(), args.toArray()).execute2Model(modelCls);
    }

    /**
     * 用unique索引,判断一个基本Bean是否冲突<br>
     * 若是一个旧model,若与缓存的一致,则不冲突<br>
     * 否则若根据unique索引能找到,则冲突<br>
     */
    private boolean unique(BaseModel model)throws Exception{
        Class<?> modelCls = model.getClass();
        BaseModel oldBaseBean = beanCache.get(modelCls).get(model.aId);
        if(oldBaseBean!=null){
            boolean equalOld = true;
            for(Field field : BaseModel.uniqueFields(modelCls)){
                if(field.get(oldBaseBean).equals(field.get(model))) continue;
                equalOld = false;
                break;
            }
            if(equalOld) return false;
        }
        
        StringBuilder sql = new StringBuilder("select count(*)<>0 from ")
            .append(modelCls.getAnnotation(Table.class).value()).append(" where ");
        List<Object> args = new LinkedList<Object>();
        boolean first = true;
        for (Field field : BaseModel.uniqueFields(modelCls)){
            if(!first) sql.append("and ");
            sql.append(field.getName()).append("=? ");
            args.add(field.get(model));
            first = false;
        }
        return db.select(sql.toString(), args.toArray()).execute2BasicOne(boolean.class);
    }
    private void set(BaseModel model){
        if(model.aId==null){
            db.insert(model).execute();
            model.seq = model.aId;
        }
        db.update(model).execute();
        beanCache.get(model.getClass()).remove(model.aId);
    }
    private void del(Class<?> modelCls, int aId){
        db.delete(modelCls, aId).execute();
        beanCache.get(modelCls).remove(aId);
    }

}
