package com.lvt4j.rbac.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.Consts.ErrCode;
import com.lvt4j.rbac.ProductAuth4Center;
import com.lvt4j.rbac.service.ProductAuthCache;
import com.lvt4j.spring.JsonResult;

@RestController
@RequestMapping("/api")
public class ApiController{

    @Autowired
    ProductAuthCache productAuthCache;
    
    @RequestMapping("/user/auth")
    public JsonResult userAuth(
            @RequestParam String proId,
            @RequestParam String userId) {
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        if(productAuth==null) return JsonResult.fail(ErrCode.NotFound, "产品["+proId+"]不存在!");
        return JsonResult.success(productAuth.getUserAuth(userId));
    }
    
    @RequestMapping("/user/allowAccess")
    public JsonResult userAllowAccess(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam String uri) {
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        if(productAuth==null) return JsonResult.fail(ErrCode.NotFound, "产品["+proId+"]不存在!");
        return JsonResult.success(productAuth.allowAccess(userId, uri));
    }
    
    @RequestMapping("/user/permit")
    public JsonResult userPermit(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam String permissionId) {
        ProductAuth4Center productAuth = productAuthCache.get(proId);
        if(productAuth==null) return JsonResult.fail(ErrCode.NotFound, "产品["+proId+"]不存在!");
        return JsonResult.success(productAuth.permit(userId, permissionId));
    }
    
}
