package com.lvt4j.rbac.web.controller.json;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.ProductAuthCaches;
import com.lvt4j.rbac.ProductAuthCaches.ProductAuth4Center;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.dto.JsonResult;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class ApiController{

    @Autowired
    private ProductAuthCaches productAuthCaches;
    
    @Read
    @RequestMapping("user/auth")
    public JsonResult userAuth(
            @RequestParam String proId,
            @RequestParam String userId) {
        ProductAuth4Center productAuth = productAuthCaches.get(proId);
        Checker.isTrue(productAuth!=null, NOT_FOUND, "产品[%s]不存在!", proId);
        return JsonResult.success(productAuth.getUserAuth(userId));
    }
    
    @Read
    @RequestMapping("user/allowAccess")
    public JsonResult userAllowAccess(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam String uri) {
        ProductAuth4Center productAuth = productAuthCaches.get(proId);
        Checker.isTrue(productAuth!=null, NOT_FOUND, "产品[%s]不存在!", proId);
        return JsonResult.success(productAuth.allowAccess(userId, uri));
    }
    
    @Read
    @RequestMapping("user/permit")
    public JsonResult userPermit(
            @RequestParam String proId,
            @RequestParam String userId,
            @RequestParam String permissionId) {
        ProductAuth4Center productAuth = productAuthCaches.get(proId);
        Checker.isTrue(productAuth!=null, NOT_FOUND, "产品[%s]不存在!", proId);
        return JsonResult.success(productAuth.permit(userId, permissionId));
    }
    
}