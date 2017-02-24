package com.lvt4j.rbac.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.basic.TDB;

@Slf4j
public class DBInterceptor implements HandlerInterceptor {

    @Autowired
    TDB db;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
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
        try {
            if(ex!=null) db.rollbackTransaction();
        } catch (Throwable e) {
            log.error("数据库回滚事务异常!", e);
        } finally {
            db.endTransaction();
        }
    }

}
