package com.lvt4j.rbac.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.RbacInterceptor.PermissionNeed;
import com.lvt4j.rbac.UserAuth;
import com.lvt4j.rbac.RbacInterceptor.RbacIgnore;

@RestController
public class TestController{

    @RequestMapping("/a/**")
    public Map<String, Object> a(
            HttpServletRequest request) {
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("uri", request.getRequestURI());
        rst.put("userAuth", request.getAttribute(UserAuth.ReqAttr));
        return rst;
    }
    
    @RequestMapping("/b/**")
    public Map<String, Object> b(
            HttpServletRequest request) {
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("uri", request.getRequestURI());
        rst.put("userAuth", request.getAttribute(UserAuth.ReqAttr));
        return rst;
    }
    @RequestMapping("/i/**")
    @RbacIgnore
    public Map<String, Object> i(
            HttpServletRequest request) {
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("uri", request.getRequestURI());
        rst.put("userAuth", request.getAttribute(UserAuth.ReqAttr));
        return rst;
    }
    
    @RequestMapping("/p/**")
    @PermissionNeed("pa")
    public Map<String, Object> p(
            HttpServletRequest request) {
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("uri", request.getRequestURI());
        rst.put("userAuth", request.getAttribute(UserAuth.ReqAttr));
        return rst;
    }
    
    
    
}
