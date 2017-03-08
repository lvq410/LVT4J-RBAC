package com.lvt4j.rbac;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.lvt4j.rbac.web.TestFilter;
import com.lvt4j.rbac.web.TestInterceptor;

/**
 * MVC配置
 * @author LV
 */
@Configuration
@EnableAutoConfiguration(exclude=DataSourceAutoConfiguration.class)
@EnableWebMvc
public class SpringConfig extends WebMvcConfigurerAdapter {

    boolean isTestFilter = false;
    String proId = "pro0";
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(isTestFilter) return;
        TestInterceptor interceptor = new TestInterceptor();
        interceptor.setProId(proId);
        interceptor.setRbacCenterSyncInterval(1);
        registry.addInterceptor(interceptor);
    }
    
    @Bean
    public Object filterRegistrationBean() {
        if(!isTestFilter) return new Object();
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        TestFilter filter = new TestFilter();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.addInitParameter("proId", proId);
        filterRegistrationBean.addInitParameter("rbacCenterSyncInterval", "1");
        return filterRegistrationBean;
    }
    
}
