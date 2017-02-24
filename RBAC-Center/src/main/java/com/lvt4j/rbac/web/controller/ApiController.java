package com.lvt4j.rbac.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.service.Cache;
import com.lvt4j.spring.JsonResult;

@RestController
@RequestMapping("/api")
public class ApiController{

    @Autowired
    Cache cacheService;
    
    @RequestMapping("/user/auth")
    public JsonResult userAuth(
            @RequestParam String proId,
            @RequestParam String userId) {
        return JsonResult.success(cacheService.getProductAuth(proId)
                .getUserAuth(userId));
    }
    
    @RequestMapping("/user/allowAccess")
    public JsonResult userAllowAccess(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam String uri) {
        return JsonResult.success(cacheService.getProductAuth(proId)
                .allowAccess(userId, uri));
    }
    
    @RequestMapping("/user/permission")
    public JsonResult userPermission(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam String permissionId) {
        return JsonResult.success();
    }
    
}
