package com.lvt4j.rbac.web.controller.json.edit;

import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.dto.MapIntStr;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Product;
import com.lvt4j.rbac.po.User;
import com.lvt4j.rbac.vo.UserVo;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月6日
 */
@RestController
@RequestMapping("edit/auth/user")
class UserAuthController extends AbstractEditController{

    @Autowired
    private UserMapper mapper;
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private UserController userController;
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @RequestParam int userAutoId,
            @RequestParam int proAutoId,
            @RequestParam(value="params",defaultValue="{}") MapIntStr params,
            @RequestParam(defaultValue="[]") ListInt roleAutoIds,
            @RequestParam(defaultValue="[]") ListInt accessAutoIds,
            @RequestParam(defaultValue="[]") ListInt permissionAutoIds){
        Product product = productMapper.selectById(proAutoId);
        Checker.isTrue(product!=null, "产品[autoId=%s]不存在", proAutoId);
        User user = mapper.selectById(userAutoId);
        Checker.isTrue(user!=null, "用户[autoId=%s]不存在", userAutoId);
        
        OpLog opLog = OpLog.create(proAutoId);
        opLog.action = "用户授权";
        UserVo orig = userController.toVo(user, proAutoId);
        opLog.orig(orig);
        
        mapper.cleanParam(userAutoId, proAutoId);
        params.forEach((paramAutoId,val)->{
            if(StringUtils.isBlank(val)) return;
            mapper.param(userAutoId, proAutoId, paramAutoId, val.trim());
        });
        mapper.cleanRole(userAutoId, proAutoId);
        IntStream.range(0, roleAutoIds.size()).forEach(i->mapper.role(userAutoId, proAutoId, roleAutoIds.get(i), i));
        mapper.cleanAccess(userAutoId, proAutoId);
        IntStream.range(0, accessAutoIds.size()).forEach(i->mapper.access(userAutoId, proAutoId, accessAutoIds.get(i), i));
        mapper.cleanPermission(userAutoId, proAutoId);
        IntStream.range(0, permissionAutoIds.size()).forEach(i->mapper.permission(userAutoId, proAutoId, permissionAutoIds.get(i), i));
        
        UserVo now = userController.toVo(user, proAutoId);
        if(orig.equals(now)) return JsonResult.success();
        
        opLog.now(now);
        oplog(opLog);
        
        //数据变更通知
        onDataChange(product.id, user.id, null);
        
        return JsonResult.success();
    }
    
    @Read
    @RequestMapping("cal")
    public JsonResult cal(
            @RequestParam int proAutoId,
            @RequestParam(defaultValue="[]") ListInt roleAutoIds,
            @RequestParam(defaultValue="[]") ListInt accessAutoIds,
            @RequestParam(defaultValue="[]") ListInt permissionAutoIds){
        return new AuthCal(true, proAutoId, roleAutoIds, accessAutoIds, permissionAutoIds).run();
    }
    
}