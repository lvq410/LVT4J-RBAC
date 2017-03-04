/**
 * @(#)TestInterceptor.java, 2017年3月2日. 
 * 
 * Copyright 2017 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.lvt4j.rbac.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.rbac.RbacInterceptor;

/**
 * @author LV
 */
public class TestInterceptor extends RbacInterceptor {

    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return request.getParameter("userId");
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
