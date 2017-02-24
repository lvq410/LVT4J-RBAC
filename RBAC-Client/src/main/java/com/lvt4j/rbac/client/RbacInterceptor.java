package com.lvt4j.rbac.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

public abstract class RbacInterceptor extends RbacBaseFilter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handleObject) throws Exception{
        if(!handle(request, response)) return false;
        
        // TODO 授权项
        return false;
    }
    
}
