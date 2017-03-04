package com.lvt4j.rbac.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController{

    @RequestMapping("/")
    public String index(){
        return "index";
    }
    
    @RequestMapping("/view/**")
    public String view(
            HttpServletRequest request){
        return request.getRequestURI().replaceFirst("/view/", "");
    }
    
}
