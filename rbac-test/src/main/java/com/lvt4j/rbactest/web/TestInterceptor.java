package com.lvt4j.rbactest.web;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lvt4j.rbac.RbacInterceptor;
import com.lvt4j.rbactest.RbacConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * @author LV
 */
@Configuration
@ConditionalOnProperty(name="test-interceptor",havingValue="true")
@ManagedResource(objectName="TestInterceptor:type=TestInterceptor")
public class TestInterceptor extends RbacInterceptor implements WebMvcConfigurer {

    @Getter(onMethod=@__({@ManagedAttribute}))
    @Setter(onMethod=@__({@ManagedAttribute}))
    private boolean onNotLogin;
    @Getter(onMethod=@__({@ManagedAttribute}))
    @Setter(onMethod=@__({@ManagedAttribute}))
    private boolean onNotRegister;
    @Getter(onMethod=@__({@ManagedAttribute}))
    @Setter(onMethod=@__({@ManagedAttribute}))
    private boolean onNotAllowAccess;
    @Getter(onMethod=@__({@ManagedAttribute}))
    @Setter(onMethod=@__({@ManagedAttribute}))
    private boolean onForbidden;
    
    @Autowired
    private RbacConfig rbacConfig;
    
    @PostConstruct
    private void init() {
        setRbacCenterAddr(rbacConfig.getCenterAddr());
        setProId(rbacConfig.getProId());
        setCacheCapacity(rbacConfig.getCacheCapacity());
        setRbacCenterTimeout(rbacConfig.getCenterTimeout());
    }
    
    @Override
    protected String getUserId(HttpServletRequest request,
            HttpServletResponse response) {
        return request.getParameter("userId");
    }
    
    @Override
    protected boolean onNotLogin(HttpServletRequest request, HttpServletResponse response) throws IOException{
        if(onNotLogin) return true;
        responeWriteForbiddenContent(response, "onNotLogin Forbidden");
        return false;
    }
    
    @Override
    protected boolean onNotRegister(HttpServletRequest request, HttpServletResponse response) throws IOException{
        if(onNotRegister) return true;
        responeWriteForbiddenContent(response, "onNotRegister Forbidden");
        return false;
    }
    
    @Override
    protected boolean onNotAllowAccess(HttpServletRequest request, HttpServletResponse response, String userId,
            String uri) throws IOException{
        if(onNotAllowAccess) return true;
        responeWriteForbiddenContent(response, "onNotAllowAccess Forbidden");
        return false;
    }
    
    @Override
    protected boolean onForbidden(HttpServletRequest request, HttpServletResponse response, String userId, String uri)
            throws IOException{
        if(onForbidden) return true;
        responeWriteForbiddenContent(response, "onForbidden Forbidden");
        return false;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }
    
}