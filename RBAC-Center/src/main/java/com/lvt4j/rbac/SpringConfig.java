package com.lvt4j.rbac;

import static com.lvt4j.rbac.Consts.SupportHandlerMethodTypes;
import static com.lvt4j.rbac.Consts.WebFolder;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.lvt4j.basic.TDB;
import com.lvt4j.spring.ControllerConfig;

/**
 * MVC配置
 * @author LV
 */
@Configuration
@EnableAutoConfiguration(exclude=DataSourceAutoConfiguration.class)
public class SpringConfig extends WebMvcConfigurerAdapter {

    @Bean
    public TDB db(@Autowired DataSource dataSource) {
        return new TDB(dataSource);
    }
    
    @Bean
    public ControllerConfig controllerConfig() {
        ControllerConfig controllerConfig = new ControllerConfig();
        controllerConfig.setPropertyEditorSupportClses(SupportHandlerMethodTypes);
        return controllerConfig;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
            WebFolder.toURI().toString());
    }
    
}