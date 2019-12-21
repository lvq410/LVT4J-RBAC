package com.lvt4j.rbac.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvt4j.rbac.data.model.Product;
import com.lvt4j.rbac.db.Read;
import com.lvt4j.rbac.service.Dao;

@Controller
public class IndexController{

    @Autowired
    private Dao dao;
    
    @Read
    @RequestMapping("/")
    public String index(
            HttpSession session){
        refreshCurPro(session);
        return "index";
    }
    
    @Read
    @RequestMapping("/view/**")
    public String view(
            HttpServletRequest request,
            HttpSession session){
        refreshCurPro(session);
        return request.getRequestURI().replaceFirst("/view/", "");
    }
    
    private void refreshCurPro(HttpSession session){
        Product curPro = (Product)session.getAttribute("curPro");
        if(curPro!=null) session.setAttribute("curPro", dao.get(Product.class, curPro.autoId));
    }
    
}
