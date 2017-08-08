package com.lvt4j.rbac;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
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

    public static PropertiesConfiguration props;
    boolean isTestFilter = true;
    String proId = "pro0";
    String rbacCenterAddr = "127.0.0.1:80";
    
    @Bean
    public PropertiesConfiguration props() throws Exception {
        props = new PropertiesConfiguration();
        File propFile = new File(Consts.ConfFolder, "application.properties");
        props.setEncoding("utf-8");
        props.setFile(propFile);
        props.setReloadingStrategy(new FileChangedReloadingStrategy());
        props.load();
        return props;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(isTestFilter) return;
        TestInterceptor interceptor = new TestInterceptor();
        interceptor.setProId(proId);
        interceptor.setRbacCenterAddr(rbacCenterAddr);
        interceptor.setRbacCenterSyncInterval(1);
        registry.addInterceptor(interceptor);
    }
    
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        if(!isTestFilter){
            Filter emptyFilter = new Filter(){
                @Override
                public void init(FilterConfig filterConfig) throws ServletException{
                }
                
                @Override
                public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                        ServletException{
                    chain.doFilter(request, response);
                }
                
                @Override public void destroy(){}
            };
            filterRegistrationBean.setFilter(emptyFilter);
            return filterRegistrationBean;
        }
        TestFilter filter = new TestFilter();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.addInitParameter("proId", proId);
        filterRegistrationBean.addInitParameter("rbacCenterAddr", rbacCenterAddr);
        filterRegistrationBean.addInitParameter("rbacCenterSyncInterval", "1");
        return filterRegistrationBean;
    }
    
}
