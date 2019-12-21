package com.lvt4j.rbac.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.lvt4j.basic.TDB;
import com.lvt4j.rbac.db.Transaction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(InterceptorOrder.Transaction)
@Configuration("TransactionInterceptor")
class TransactionInterceptor extends WebMvcConfigurerAdapter implements HandlerInterceptor,HandlerExceptionResolver {

    @Autowired
    private TDB db;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/edit/**");
    }
    
    @Override
    public void extendHandlerExceptionResolvers(
            List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(0, this);
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object rawHandler) throws Exception {
        if(!(rawHandler instanceof HandlerMethod)) return true;
        HandlerMethod handler = (HandlerMethod)rawHandler;
        if(!handler.hasMethodAnnotation(Transaction.class)) return true;
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
            HttpServletResponse response, Object rawHandler, Exception ex)
            throws Exception {
        resolveException(request, response, rawHandler, ex);
    }
    
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
            HttpServletResponse response, Object rawHandler, Exception ex) {
        if(!(rawHandler instanceof HandlerMethod)) return null;
        HandlerMethod handler = (HandlerMethod)rawHandler;
        if(!handler.hasMethodAnnotation(Transaction.class)) return null;
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
        return null;
    }
    
}