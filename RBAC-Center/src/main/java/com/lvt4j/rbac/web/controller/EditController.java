package com.lvt4j.rbac.web.controller;

import static com.lvt4j.rbac.Consts.CookieName_CurProAutoId;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.basic.TPager;
import com.lvt4j.rbac.Consts.ErrCode;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.model.Access;
import com.lvt4j.rbac.data.model.OpLog;
import com.lvt4j.rbac.data.model.Permission;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.data.model.Role;
import com.lvt4j.rbac.data.model.User;
import com.lvt4j.rbac.db.Read;
import com.lvt4j.rbac.db.Transaction;
import com.lvt4j.rbac.db.Write;
import com.lvt4j.rbac.service.Dao;
import com.lvt4j.rbac.service.Dao.AuthCalRst;
import com.lvt4j.spring.JsonResult;

import net.sf.json.JSONObject;


/**
 * 编辑用接口
 * @author LV
 */
@RestController
@RequestMapping("/edit")
class EditController {

    @Autowired
    private Dao dao;
    
    @Write
    @RequestMapping("/curProSet")
    public JsonResult curProSet(
            HttpServletResponse res,
            @RequestParam int proAutoId) throws Exception {
        Product pro = dao.get(Product.class, proAutoId);
        String proAutoIdStr = pro==null?EMPTY:String.valueOf(pro.autoId);
        Cookie cookie = new Cookie(CookieName_CurProAutoId, proAutoIdStr);
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/");
        res.addCookie(cookie);
        return JsonResult.success();
    }
    
    @Read
    @SuppressWarnings("unchecked")
    @RequestMapping("/{modelName}/list")
    public JsonResult baseList(
            @PathVariable String modelName,
            @RequestParam(required=false) Integer proAutoId,
            @RequestParam(required=false) Integer roleAutoId,
            @RequestParam(required=false) Integer accessAutoId,
            @RequestParam(required=false) Integer permissionAutoId,
            @RequestParam(required=false) boolean needAuth,
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) TPager pager){
        Class<?> modelCls = Model.getModelCls(modelName);
        Pair<Long, List<? extends Model>> pair = dao.list(modelName, proAutoId, roleAutoId, accessAutoId, permissionAutoId, keyword, pager);
        JsonResult rst = JsonResult.success().dataPut("count", pair.getLeft()).dataPut("models", pair.getRight());
        if(!needAuth || pair.getRight().isEmpty()) return rst;
        if(Role.class!=modelCls
                && User.class!=modelCls) return rst;
        if(User.class==modelCls){
            for(User user : (List<User>)pair.getRight()){
                user.params = dao.params(modelName, proAutoId, user.autoId);
                user.roles = dao.auths(modelName, Role.class, proAutoId, user.autoId);
                user.accesses = dao.auths(modelName, Access.class, proAutoId, user.autoId);
                user.permissions = dao.auths(modelName, Permission.class, proAutoId, user.autoId);
            }
        }else if(Role.class==modelCls){
            for(Role role : (List<Role>)pair.getRight()){
                role.accesses = dao.auths(modelName, Access.class, proAutoId, role.autoId);
                role.permissions = dao.auths(modelName, Permission.class, proAutoId, role.autoId);
            }
        }
        return rst;
    }
    @Read
    @RequestMapping("/{modelName}/get")
    public JsonResult baseGet(
            @PathVariable String modelName,
            @RequestParam(required=false) Integer autoId,
            @RequestParam(required=false) String id,
            @RequestParam(required=false) boolean needAuth,
            @RequestParam(required=false) Integer proAutoId) {
        Class<? extends Model> modelCls = Model.getModelCls(modelName);
        Object model = null;
        if(autoId!=null) model = dao.get(modelCls, autoId);
        else if(id!=null) model = dao.uniqueGet(modelCls, id);
        else throw new IllegalArgumentException("autoId 或 id 参数必填一项");
        if(!needAuth) return JsonResult.success(model);
        if(Role.class!=modelCls
                && User.class!=modelCls) return JsonResult.success(model);
        Validate.notNull(proAutoId, "needAuth为true时proAutoId参数必须");
        if(User.class==modelCls){
            User user = (User) model;
            user.params = dao.params(modelName, proAutoId, user.autoId);
            user.roles = dao.auths(modelName, Role.class, proAutoId, user.autoId);
            user.accesses = dao.auths(modelName, Access.class, proAutoId, user.autoId);
            user.permissions = dao.auths(modelName, Permission.class, proAutoId, user.autoId);
        }else if(Role.class==modelCls){
            Role role = (Role) model;
            role.accesses = dao.auths(modelName, Access.class, proAutoId, role.autoId);
            role.permissions = dao.auths(modelName, Permission.class, proAutoId, role.autoId);
        }
        return JsonResult.success(model);
    }
    @Write
    @Transaction
    @RequestMapping("/{modelName}/set")
    public JsonResult baseSet(HttpServletRequest req,
            @RequestAttribute("operator") String operator,
            @PathVariable String modelName,
            @RequestParam(required=false) Integer proAutoId,
            @RequestParam Map<String, String> modelData,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        Class<? extends Model> modelCls = Model.getModelCls(modelName);
        Model model = modelCls.newInstance();
        model.set(modelData);
        Pair<Boolean, Model> duplicateRst = dao.isDuplicated(model);
        if(duplicateRst.getLeft()) return JsonResult.fail(ErrCode.Duplicate);
        
        OpLog opLog = new OpLog();
        opLog.operator = operator;
        opLog.ip = reqIp(req);
        opLog.action = (duplicateRst.getRight()==null?"新增":"修改") + Model.getModelDes(modelCls);
        opLog.time = new Date();
        opLog.proAutoId = proAutoId;
        if(Role.class==modelCls && duplicateRst.getRight()!=null){
            Role role = (Role) duplicateRst.getRight();
            role.accesses = dao.auths(modelName, Access.class, proAutoId, role.autoId);
            role.permissions = dao.auths(modelName, Permission.class, proAutoId, role.autoId);
        }
        opLog.setOrig(duplicateRst.getRight());
        
        dao.set(model);
        if(Role.class==modelCls){
            Role role = (Role) model;
            dao.authsSet(modelName, Access.class, proAutoId, (int)model.get("autoId"), accessAutoIds);
            dao.authsSet(modelName, Permission.class, proAutoId, (int)model.get("autoId"), permissionAutoIds);
            role.accesses = dao.auths(modelName, Access.class, proAutoId, role.autoId);
            role.permissions = dao.auths(modelName, Permission.class, proAutoId, role.autoId);
        }
        
        if(Product.class==modelCls) opLog.proAutoId = (Integer) model.get("autoId");
        opLog.setNow(model);
        
        oplog(opLog);
        return JsonResult.success(model);
    }
    @Write
    @Transaction
    @RequestMapping("/{modelName}/del")
    public JsonResult baseDel(HttpServletRequest req,
            @RequestAttribute("operator") String operator,
            @PathVariable String modelName,
            @RequestParam int autoId)throws Exception{
        Class<? extends Model> modelCls = Model.getModelCls(modelName);
        Model orig = dao.get(modelCls, autoId);
        if(orig==null) return JsonResult.success();
        
        
        OpLog opLog = new OpLog();
        opLog.operator = operator;
        opLog.ip = reqIp(req);
        opLog.action = "删除" + Model.getModelDes(modelCls);
        opLog.time = new Date();
        if(Product.class==modelCls) opLog.proAutoId = (Integer) orig.get("autoId");
        else if (User.class!=modelCls) opLog.proAutoId = (Integer) orig.get("proAutoId");
        if(Role.class==modelCls){
            Role role = (Role) orig;
            role.accesses = dao.auths(modelName, Access.class, role.proAutoId, role.autoId);
            role.permissions = dao.auths(modelName, Permission.class, role.proAutoId, role.autoId);
        }
        opLog.setOrig(orig);
        
        dao.del(modelCls, autoId);
        
        oplog(opLog);
        return JsonResult.success();
    }
    @Write
    @Transaction
    @RequestMapping("/{modelName}/sort")
    public JsonResult baseSort(HttpServletRequest req,
            @RequestAttribute("operator") String operator,
            @PathVariable String modelName,
            @RequestParam("autoIds") int[] autoIds)throws Exception{
        if(ArrayUtils.isEmpty(autoIds)) return JsonResult.success();
        Class<? extends Model> modelCls = Model.getModelCls(modelName);
        
        Pair<List<Model>, List<Integer>> pair = dao.sort(modelName, autoIds);
        
        OpLog opLog = new OpLog();
        opLog.operator = operator;
        opLog.ip = reqIp(req);
        opLog.action = "排序" + Model.getModelDes(modelCls);
        opLog.time = new Date();
        opLog.setOrig(pair.getLeft());
        opLog.setNow(pair.getRight());
        if(Product.class!=modelCls && User.class!=modelCls){
            opLog.proAutoId = (Integer) pair.getLeft().get(0).get("proAutoId");
        }
        
        oplog(opLog);
        return JsonResult.success();
    }
    
    @RequestMapping("/access/patternMatch")
    public JsonResult accessPatternMatch(
            @RequestParam String pattern,
            @RequestParam String uri){
        return JsonResult.success(uri.matches(pattern));
    }
    
    @Read
    @RequestMapping("/auth/visitor/get")
    public JsonResult authVisitorGet(
            @RequestParam int proAutoId){
        String modelName = "visitor";
        return JsonResult.success()
                .dataPut("params", dao.params(modelName, proAutoId, null))
                .dataPut("roles", dao.auths(modelName, Role.class, proAutoId, null))
                .dataPut("accesses", dao.auths(modelName, Access.class, proAutoId, null))
                .dataPut("permissions", dao.auths(modelName, Permission.class, proAutoId, null));
    }
    @Write
    @Transaction
    @RequestMapping("/auth/visitor/set")
    public JsonResult authVisitorSet(HttpServletRequest req,
            @RequestAttribute("operator") String operator,
            @RequestParam int proAutoId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) int[] roleAutoIds,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        
        OpLog opLog = new OpLog();
        opLog.operator = operator;
        opLog.ip = reqIp(req);
        opLog.action = "游客授权";
        opLog.time = new Date();
        opLog.setOrig(authVisitorGet(proAutoId).data());
        opLog.proAutoId = proAutoId;
        
        String modelName = "visitor";
        dao.paramsSet(modelName, proAutoId, null, params);
        dao.authsSet(modelName, Role.class, proAutoId, null, roleAutoIds);
        dao.authsSet(modelName, Access.class, proAutoId, null, accessAutoIds);
        dao.authsSet(modelName, Permission.class, proAutoId, null, permissionAutoIds);
        dao.productNotify(proAutoId);
        
        opLog.setNow(authVisitorGet(proAutoId).data());
        
        oplog(opLog);
        return JsonResult.success();
    }
    @Read
    @RequestMapping("/auth/visitor/cal")
    public JsonResult authVisitorCal(
            @RequestParam(required=false) int[] roleAutoIds,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        AuthCalRst authCalRst = dao.authCal(null, roleAutoIds, accessAutoIds, permissionAutoIds);
        return JsonResult.success()
                .dataPut("allAccesses", authCalRst.getAuthDescs(Access.class))
                .dataPut("allPermissions", authCalRst.getAuthDescs(Permission.class));
    }
    
    @Write
    @Transaction
    @RequestMapping("/auth/user/set")
    public JsonResult authUserSet(HttpServletRequest req,
            @RequestAttribute("operator") String operator,
            @RequestParam int proAutoId,
            @RequestParam int userAutoId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) int[] roleAutoIds,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        String modelName = "user";
        
        OpLog opLog = new OpLog();
        opLog.operator = operator;
        opLog.ip = reqIp(req);
        opLog.action = "用户授权";
        opLog.time = new Date();
        opLog.setOrig(baseGet(modelName, userAutoId, null, true, proAutoId).data());
        opLog.proAutoId = proAutoId;
        
        dao.paramsSet(modelName, proAutoId, userAutoId, params);
        dao.authsSet(modelName, Role.class, proAutoId, userAutoId, roleAutoIds);
        dao.authsSet(modelName, Access.class, proAutoId, userAutoId, accessAutoIds);
        dao.authsSet(modelName, Permission.class, proAutoId, userAutoId, permissionAutoIds);
        dao.productNotify(proAutoId);
        
        opLog.setNow(baseGet(modelName, userAutoId, null, true, proAutoId).data());
        oplog(opLog);
        return JsonResult.success();
    }
    @Read
    @RequestMapping("/auth/user/cal")
    public JsonResult authUserCal(
            @RequestParam int proAutoId,
            @RequestParam(required=false) int[] roleAutoIds,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        AuthCalRst authCalRst = dao.authCal(proAutoId, roleAutoIds, accessAutoIds, permissionAutoIds);
        return JsonResult.success()
                .dataPut("allRoles", authCalRst.getAuthDescs(Role.class))
                .dataPut("allAccesses", authCalRst.getAuthDescs(Access.class))
                .dataPut("allPermissions", authCalRst.getAuthDescs(Permission.class));
    }
    
    private void oplog(OpLog opLog) {
        dao.oplog(opLog);
    }
    
    @Read
    @RequestMapping("/oplogs")
    public JsonResult oplogs(
            OpLog.Query query,
            @RequestParam boolean ascOrDesc,
            @RequestParam TPager pager) {
        Triple<Long, List<OpLog>, Map<Integer, Product>> pair = dao.oplogs(query, ascOrDesc, pager);
        return JsonResult.success()
            .dataPut("count", pair.getLeft())
            .dataPut("oplogs", pair.getMiddle())
            .dataPut("pros", pair.getRight());
    }
    
    private String reqIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip.split(",")[0];
        ip = req.getHeader("Proxy-Client-IP");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = req.getHeader("WL-Proxy-Client-IP");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        ip = req.getHeader("X-Real-IP");
        if(StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        return req.getRemoteAddr();
    }
}