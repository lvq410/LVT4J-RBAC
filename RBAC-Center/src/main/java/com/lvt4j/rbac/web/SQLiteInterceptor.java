package com.lvt4j.rbac.web;

import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.lvt4j.rbac.db.Read;
import com.lvt4j.rbac.db.Write;

import lombok.extern.slf4j.Slf4j;

/**
 * SQLite数据库只能单线程处理，上个锁
 * @author LV
 */
@Slf4j
@Order(InterceptorOrder.SQLite)
@Configuration("SQLiteInterceptor")
@ConditionalOnProperty(name="db.type",havingValue="sqlite")
class SQLiteInterceptor extends WebMvcConfigurerAdapter implements HandlerInterceptor {

    private ReentrantLock lock = new ReentrantLock();
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/api/**","/edit/**","/inner/**");
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object rawHandler) throws Exception {
        if(!(rawHandler instanceof HandlerMethod)) return true;
        HandlerMethod handler = (HandlerMethod)rawHandler;
        if(!handler.hasMethodAnnotation(Read.class) && !handler.hasMethodAnnotation(Write.class)) return true;
        lock.lock();
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
        if(!handler.hasMethodAnnotation(Read.class) && !handler.hasMethodAnnotation(Write.class)) return;
        try{
            lock.unlock();
        }catch(Exception e){
            log.error("SQLite锁释放失败!", e);
        }
    }
    
}