package com.lvt4j.rbac.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.UserAuth;

@RestController
public class TestController{

    @RequestMapping("/**")
    public Map<String, Object> test(
            HttpServletRequest request) {
        Map<String, Object> rst = new HashMap<String, Object>();
        rst.put("uri", request.getRequestURI());
        rst.put("userAuth", request.getAttribute(UserAuth.ReqAttr));
        return rst;
    }
    
}
