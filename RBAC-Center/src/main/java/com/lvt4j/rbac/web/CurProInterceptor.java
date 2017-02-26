package com.lvt4j.rbac.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.rbac.ProductAuthImp;
import com.lvt4j.rbac.data.bean.Product;
import com.lvt4j.rbac.service.Cache;

public class CurProInterceptor implements HandlerInterceptor {

    @Autowired
    Cache cacheService;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        String curProId = request.getParameter("curProId");
        HttpSession session = request.getSession();
        if(curProId!=null) {
            ProductAuthImp productAuth = cacheService.getProductAuth(curProId);
            session.setAttribute("curPro", productAuth==null?null:productAuth.product);
        } else {
            Product curPro = (Product) session.getAttribute("curPro");
            if(curPro!=null){
                ProductAuthImp productAuth = cacheService.getProductAuth(curPro.id);
                session.setAttribute("curPro", productAuth==null?null:productAuth.product);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
