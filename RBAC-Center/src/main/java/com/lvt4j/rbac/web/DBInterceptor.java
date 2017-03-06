package com.lvt4j.rbac.web;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.basic.TArr;
import com.lvt4j.basic.TDB;
import com.lvt4j.rbac.data.Transaction;

@Slf4j
public class DBInterceptor implements HandlerInterceptor {

    @Autowired
    TDB db;
    @Autowired
    ReentrantLock editLock;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object rawHandler) throws Exception {
        if(!(rawHandler instanceof HandlerMethod)) return true;
        HandlerMethod handler = (HandlerMethod)rawHandler;
        if(!handler.hasMethodAnnotation(Transaction.class)) return true;
        editLock.lock();
        db.beginTransaction();
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
        try{
            if(ex != null) db.rollbackTransaction();
        }catch(Throwable e){
            log.error("数据库回滚事务异常!", e);
        }
        try{
            db.endTransaction();
        }catch(Exception e){
            log.error("数据库提交事务异常!", e);
        }
        try{
            if(editLock.isLocked()) editLock.unlock();
        }catch(Exception e){
            log.error("释放编辑锁异常!", e);
        }
    }
    
}
