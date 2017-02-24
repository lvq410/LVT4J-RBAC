package com.lvt4j.rbac.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController{

    @RequestMapping("/")
    public String index(){
        return "index";
    }
    
    @RequestMapping("/view")
    public String view(
            @RequestParam String path){
        return path;
    }
    
}
