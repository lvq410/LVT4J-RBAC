package com.lvt4j.rbac.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.RbacInterceptor.PermissionNeed;
import com.lvt4j.rbac.RbacInterceptor.RbacIgnore;
import com.lvt4j.rbac.RbacInterceptor.RegisteredIgnore;
import com.lvt4j.rbac.UserAuth;

@RestController
public class TestController{

    @RbacIgnore
    @RequestMapping("/ignore/**")
    public Map<String, Object> ignore(
            HttpServletRequest request) {
        return defRet(request);
    }
    
    @RegisteredIgnore
    @RequestMapping("/regIgnore/**")
    public Map<String, Object> regIgnore(
            HttpServletRequest request) {
        return defRet(request);
    }
    
    @RequestMapping("/noPermit/**")
    public Map<String, Object> noPermit(
            HttpServletRequest request) {
        return defRet(request);
    }
    @RequestMapping("/permit/**")
    @PermissionNeed("testPermit")
    public Map<String, Object> permit(
            HttpServletRequest request) {
        return defRet(request);
    }
    
    private Map<String, Object> defRet(HttpServletRequest request){
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("uri", request.getRequestURI());
        rst.put("userAuth", request.getAttribute(UserAuth.ReqAttr));
        return rst;
    }
}
