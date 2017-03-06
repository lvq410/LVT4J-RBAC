package com.lvt4j.rbac.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.basic.TCollection.TAutoMap;
import com.lvt4j.basic.TCollection.TAutoMap.ValueBuilder;
import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TCollection;
import com.lvt4j.basic.TPager;
import com.lvt4j.basic.TReflect;
import com.lvt4j.rbac.Consts.Err;
import com.lvt4j.rbac.Consts;
import com.lvt4j.rbac.ProductAuth4Center;
import com.lvt4j.rbac.data.Transaction;
import com.lvt4j.rbac.data.base.BaseModel;
import com.lvt4j.rbac.data.base.ProCtrlModel;
import com.lvt4j.rbac.data.base.SeqModel;
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
import com.lvt4j.spring.ControllerConfig;
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
    ControllerConfig controllerConfig;
    
    @Autowired
    ProductAuthCache productAuthCache;
    
    TAutoMap<Class<?>, TAutoMap<Integer, Object>> modelCache = new TAutoMap<Class<?>, TAutoMap<Integer, Object>>(
            new ValueBuilder<Class<?>, TAutoMap<Integer, Object>>(){
        private static final long serialVersionUID = 1L;
        @Override
        public TAutoMap<Integer, Object> build(Class<?> modelCls){
            return new TAutoMap<Integer, Object>(new ValueBuilder<Integer, Object>(){
                private static final long serialVersionUID = 1L;
                @Override
                public Object build(Integer aId){
                    return db.get(modelCls, aId).execute();
                }
            });
        }
    });
    
    @RequestMapping("/curProSet")
    public JsonResult curProSet(
            HttpSession session,
            @RequestParam int proAId){
        session.setAttribute("curPro", modelCache.get(Product.class).get(proAId));
        return JsonResult.success();
    }
    
    @RequestMapping("/base/list")
    public JsonResult baseList(
            @RequestParam String modelName,
            @RequestParam Integer proAId,
            @RequestParam(required=false) String keyword,
            @RequestParam TPager pager){
        return JsonResult.success(list(Consts.AllBaseModelCls.get(modelName), proAId, keyword, pager));
    }
    @RequestMapping("/base/set")
    @Transaction
    public JsonResult baseSet(
            @RequestParam String modelName,
            Map<String, String> modelData)throws Exception{
        BeanWrapper beanWrapper = new BeanWrapperImpl(Consts.AllBaseModelCls.get(modelName));
        controllerConfig.registerCustomEditors(beanWrapper);
        beanWrapper.setPropertyValues(modelData);
        BaseModel model = (BaseModel)beanWrapper.getWrappedInstance();
        if(unique(model)) return JsonResult.fail(Err.Duplicate);
        set(model);
        return JsonResult.success();
    }
    @RequestMapping("/base/del")
    @Transaction
    public JsonResult baseDel(
            @RequestParam String modelName,
            @RequestParam int aId){
        Class<?> modelCls = Consts.AllBaseModelCls.get(modelName);
        del(modelCls, aId);
        return JsonResult.success();
    }
    @RequestMapping("/base/sort")
    @Transaction
    public JsonResult productDel(
            @RequestParam String modelName,
            @RequestParam("aId") int[] aIds){
        Class<?> modelCls = Consts.AllBaseModelCls.get(modelName);
        List<Integer> seqs = new ArrayList<Integer>(aIds.length);
        List<Integer> aIdsList = new ArrayList<Integer>(aIds.length);
        for(int aId : aIds){
            aIdsList.add(aId);
            if(seqs.contains(aId)) return JsonResult.fail(Err.NotFound);
            SeqModel model = (SeqModel)modelCache.get(modelCls).get(aId);
            if(model==null) return JsonResult.fail(Err.NotFound);
            seqs.add(model.seq);
        }
        if(!TCollection.isEqual(aIdsList, seqs)) return JsonResult.fail(Err.NotFound);
        for(int i = 0; i < aIds.length; i++){
            db.executeSQL("update "+modelCls.getAnnotation(Table.class).value()+" set seq=? where aId=?",
                    seqs.get(i), aIds[i]).execute();
        }
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
            for(Field field : Consts.LikeFields.get(modelCls)){
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
    private boolean unique(Object rawModel)throws Exception{
        Class<?> modelCls = rawModel.getClass();
        BaseModel model = (BaseModel)rawModel;
        BaseModel oldBaseModel = (BaseModel)modelCache.get(modelCls).get(model.aId);
        if(oldBaseModel!=null){
            boolean equalOld = true;
            for(Field field : Consts.UniqueFields.get(modelCls)){
                if(field.get(oldBaseModel).equals(field.get(model))) continue;
                equalOld = false;
                break;
            }
            if(equalOld) return false;
        }
        
        StringBuilder sql = new StringBuilder("select count(*)<>0 from ")
            .append(modelCls.getAnnotation(Table.class).value()).append(" where ");
        List<Object> args = new LinkedList<Object>();
        boolean first = true;
        for (Field field : Consts.UniqueFields.get(modelCls)){
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
        if(model instanceof Product){
            productNotify(model.aId);
        } else if (model instanceof ProCtrlModel) {
            productNotify(((ProCtrlModel)model).proAId);
        }
        modelCache.get(model.getClass()).remove(model.aId);
    }
    private void del(Class<?> modelCls, int aId){
        BaseModel model = (BaseModel)modelCache.get(modelCls).get(aId);
        if(model==null) return;
        if(User.class==modelCls){
            productNotify(db.select(
                    "select distinct P.proAId from user_param UP inner join param P on UP.paramAId=P.aId where userAId=? "
                    +"union select distinct proAId from user_role where userAId=? "
                    +"union select distinct proAId from user_access where userAId=? "
                    +"union select distinct proAId from user_permission where userAId=?",
                    aId, aId, aId, aId).execute2Basic(Integer.class)
                    .toArray(new Integer[]{}));
        }
        db.delete(model).execute();
        if(model instanceof Product){
            productNotify(model.aId);
        } else if (model instanceof ProCtrlModel) {
            productNotify(((ProCtrlModel)model).proAId);
        }
        modelCache.get(modelCls).remove(aId);
    }
    private void productNotify(Integer... proAIds){
        if(proAIds==null){
            db.executeSQL("update product set lastModify=?",
                    System.currentTimeMillis()).execute();
            productAuthCache.clear();
            modelCache.get(Product.class).clear();
            return;
        }
        for(int proAId : proAIds){
            db.executeSQL("update product set lastModify=? where aId=?",
                    System.currentTimeMillis(), proAId).execute();
            // TODO
            //productAuthCache.invalidate(proAId);
            modelCache.get(Product.class).remove(proAId);
        }
    }
}
