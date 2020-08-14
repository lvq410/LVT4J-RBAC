package com.lvt4j.rbactest.web;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.lvt4j.rbac.RbacFilter;
import com.lvt4j.rbactest.RbacConfig;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author LV on 2020年8月10日
 */
@Configuration
@ConditionalOnProperty(name="test-filter",havingValue="true")
@ManagedResource(objectName="TestFilter:type=TestFilter")
public class TestFilter extends FilterRegistrationBean<TestFilter.Filter> {

    @Getter(onMethod=@__({@ManagedAttribute}))
    @Setter(onMethod=@__({@ManagedAttribute}))
    private boolean onNotLogin;
    @Getter(onMethod=@__({@ManagedAttribute}))
    @Setter(onMethod=@__({@ManagedAttribute}))
    private boolean onNotRegister;
    
    @Autowired
    private RbacConfig rbacConfig;
    
    @PostConstruct
    private void init() {
        addInitParameter("rbacCenterAddr", rbacConfig.getCenterAddr());
        addInitParameter("proId", rbacConfig.getProId());
        addInitParameter("cacheCapacity", rbacConfig.getCacheCapacity()+"");
        addInitParameter("rbacCenterTimeout", rbacConfig.getCenterTimeout()+"");
        setFilter(new Filter());
    }
    
    class Filter extends RbacFilter {
        
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
        
    }
    
}