package com.lvt4j.rbac.web.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.basic.TPager;
import com.lvt4j.rbac.Consts;
import com.lvt4j.rbac.Consts.ErrCode;
import com.lvt4j.rbac.data.Model;
import com.lvt4j.rbac.data.Transaction;
import com.lvt4j.rbac.data.model.Access;
import com.lvt4j.rbac.data.model.Permission;
import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.data.model.Role;
import com.lvt4j.rbac.data.model.User;
import com.lvt4j.rbac.service.Dao;
import com.lvt4j.rbac.service.Dao.AuthCalRst;
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
    Lock editLock;
    
    @Autowired
    ControllerConfig controllerConfig;
    
    @Autowired
    ProductAuthCache productAuthCache;
    
    @Autowired
    Dao dao;
    
    @RequestMapping("/curProSet")
    public JsonResult curProSet(
            HttpSession session,
            @RequestParam int proAutoId){
        session.setAttribute("curPro", dao.get(Product.class, proAutoId));
        return JsonResult.success();
    }
    
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
        Class<?> modelCls = Consts.AllBaseModelCls.get(modelName);
        List<?> list = dao.list(modelName, proAutoId, roleAutoId, accessAutoId, permissionAutoId, keyword, pager);
        if(!needAuth || list.isEmpty()) return JsonResult.success(list);
        if(Role.class!=modelCls
                && User.class!=modelCls) return JsonResult.success(list);
        if(User.class==modelCls){
            for(User user : (List<User>)list){
                user.params = dao.params(modelName, proAutoId, user.autoId);
                user.roles = dao.auths(modelName, Role.class, proAutoId, user.autoId);
                user.accesses = dao.auths(modelName, Access.class, proAutoId, user.autoId);
                user.permissions = dao.auths(modelName, Permission.class, proAutoId, user.autoId);
            }
        }else if(Role.class==modelCls){
            for(Role role : (List<Role>)list){
                role.accesses = dao.auths(modelName, Access.class, proAutoId, role.autoId);
                role.permissions = dao.auths(modelName, Permission.class, proAutoId, role.autoId);
            }
        }
        return JsonResult.success(list);
    }
    @RequestMapping("/{modelName}/set")
    @Transaction
    public JsonResult baseSet(
            @PathVariable String modelName,
            @RequestParam(required=false) Integer proAutoId,
            @RequestParam Map<String, String> modelData,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        Class<? extends Model> modelCls = Consts.AllBaseModelCls.get(modelName);
        Model model = modelCls.newInstance();
        model.set(modelData);
        if(dao.isDuplicated(model)) return JsonResult.fail(ErrCode.Duplicate);
        dao.set(model);
        if(Role.class==modelCls){
            dao.authsSet(modelName, Access.class, proAutoId, (int)model.get("autoId"), accessAutoIds);
            dao.authsSet(modelName, Permission.class, proAutoId, (int)model.get("autoId"), permissionAutoIds);
        }
        return JsonResult.success();
    }
    @RequestMapping("/{modelName}/del")
    @Transaction
    public JsonResult baseDel(
            @PathVariable String modelName,
            @RequestParam int autoId)throws Exception{
        Class<? extends Model> modelCls = Consts.AllBaseModelCls.get(modelName);
        dao.del(modelCls, autoId);
        return JsonResult.success();
    }
    @RequestMapping("/{modelName}/sort")
    @Transaction
    public JsonResult baseSort(
            @PathVariable String modelName,
            @RequestParam("autoIds") int[] autoIds)throws Exception{
        dao.sort(modelName, autoIds);
        return JsonResult.success();
    }
    
    @RequestMapping("/access/patternMatch")
    public JsonResult accessPatternMatch(
            @RequestParam String pattern,
            @RequestParam String uri){
        return JsonResult.success(uri.matches(pattern));
    }
    
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
    @RequestMapping("/auth/visitor/set")
    @Transaction
    public JsonResult authVisitorSet(
            @RequestParam int proAutoId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) int[] roleAutoIds,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        String modelName = "visitor";
        dao.paramsSet(modelName, proAutoId, null, params);
        dao.authsSet(modelName, Role.class, proAutoId, null, roleAutoIds);
        dao.authsSet(modelName, Access.class, proAutoId, null, accessAutoIds);
        dao.authsSet(modelName, Permission.class, proAutoId, null, permissionAutoIds);
        dao.productNotify(proAutoId);
        return JsonResult.success();
    }
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
    
    @RequestMapping("/auth/user/set")
    @Transaction
    public JsonResult authUserSet(
            @RequestParam int proAutoId,
            @RequestParam int userAutoId,
            @RequestParam(value="params",required=false) JSONObject params,
            @RequestParam(required=false) int[] roleAutoIds,
            @RequestParam(required=false) int[] accessAutoIds,
            @RequestParam(required=false) int[] permissionAutoIds)throws Exception{
        String modelName = "user";
        dao.paramsSet(modelName, proAutoId, userAutoId, params);
        dao.authsSet(modelName, Role.class, proAutoId, userAutoId, roleAutoIds);
        dao.authsSet(modelName, Access.class, proAutoId, userAutoId, accessAutoIds);
        dao.authsSet(modelName, Permission.class, proAutoId, userAutoId, permissionAutoIds);
        dao.productNotify(proAutoId);
        return JsonResult.success();
    }
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
    
}
