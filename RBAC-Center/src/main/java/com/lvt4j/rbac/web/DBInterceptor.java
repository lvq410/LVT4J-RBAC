package com.lvt4j.rbac.web;

import java.util.concurrent.locks.ReentrantReadWriteLock;

//import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lvt4j.basic.TDB;
import com.lvt4j.rbac.data.Transaction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBInterceptor implements HandlerInterceptor {

    @Autowired
    private TDB db;
    
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object rawHandler) throws Exception {
        if(!(rawHandler instanceof HandlerMethod)) return true;
        HandlerMethod handler = (HandlerMethod)rawHandler;
        if(handler.hasMethodAnnotation(Transaction.class)) db.beginTransaction();
        if(handler.hasMethodAnnotation(Read.class)) lock.readLock().lock();
        if(handler.hasMethodAnnotation(Write.class)) lock.writeLock().lock();
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object rawHandler, Exception ex)
            throws Exception {
        if(!(rawHandler instanceof HandlerMethod)) return;
        HandlerMethod handler = (HandlerMethod)rawHandler;
        if(handler.hasMethodAnnotation(Transaction.class)){
            try{
                if(ex!=null) db.rollbackTransaction();
            }catch(Throwable e){
                log.error("数据库回滚事务异常!", e);
            }
            try{
                db.endTransaction();
            }catch(Exception e){
                log.error("数据库提交事务异常!", e);
            }
        }
        if(handler.hasMethodAnnotation(Read.class)){
            try{
                lock.readLock().unlock();
            }catch(Exception e){
                log.error("读锁释放失败!", e);
            }
        }
        if(handler.hasMethodAnnotation(Write.class)) {
            try{
                lock.writeLock().unlock();
            }catch(Exception e){
                log.error("写锁释放失败!", e);
            }
        }
    }
    
}
