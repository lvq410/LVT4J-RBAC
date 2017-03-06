package com.lvt4j.rbac;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;
import org.sqlite.SQLiteDataSource;

import com.lvt4j.basic.TDB;
import com.lvt4j.rbac.web.AdminInterceptor;
import com.lvt4j.rbac.web.CurProInterceptor;
import com.lvt4j.rbac.web.DBInterceptor;
import com.lvt4j.spring.ControllerConfig;

/**
 * MVC配置
 * @author LV
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableAutoConfiguration(exclude=DataSourceAutoConfiguration.class)
@EnableWebMvc
public class SpringConfig extends WebMvcConfigurerAdapter {

    @Bean
    public SQLiteDataSource dataSource() throws Throwable {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:"+Consts.DBFileName);
        dataSource.setEnforceForeignKeys(true);
        dataSource.setIncrementalVacuum(10);
        return dataSource;
    }
    
    @Bean
    public TDB db(@Autowired DataSource dataSource) {
        TDB db = new TDB(dataSource);
        if(Config.isDebug) db.openPrintSQL();
        Consts.DB = db;
        return db;
    }
    
    @Bean
    public ReentrantLock editLock(){
        return new ReentrantLock();
    }
    
    @Bean
    public ControllerConfig controllerConfig() {
        ControllerConfig controllerConfig = new ControllerConfig();
        controllerConfig.setPropertyEditorSupportClses(Consts.SupportHandlerMethodTypes);
        return controllerConfig;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor()).addPathPatterns("", "/", "/edit/**", "/view/**");
        registry.addInterceptor(dbInterceptor()).addPathPatterns("/edit/**");
    }
    
    @Bean
    public AdminInterceptor authInterceptor() {
        return new AdminInterceptor();
    }
    
    @Bean
    public DBInterceptor dbInterceptor() {
        return new DBInterceptor();
    }
    
    @Bean
    public CurProInterceptor curProInterceptor() {
        return new CurProInterceptor();
    }
    
//    @Bean
//    public VelocityConfigurer velocityConfig() {
//        VelocityConfigurer velocityConfig = new VelocityConfigurer();
//        Map<String, Object> velocityPropertiesMap = new HashMap<String, Object>();
//        velocityPropertiesMap.put("resource.loader", "custom");
//        velocityPropertiesMap.put("custom.resource.loader.class", "com.lvt4j.rbac.VelocityTplLoader");
//        velocityPropertiesMap.put("input.encoding", "utf-8");
//        velocityPropertiesMap.put("output.encoding", "utf-8");
//        velocityConfig.setVelocityPropertiesMap(velocityPropertiesMap);
//        return velocityConfig;
//    }
//    @Bean
//    public ViewResolver viewResolver() {
//        VelocityViewResolver viewResolver = new VelocityViewResolver();
//        viewResolver.setCache(true);
//        viewResolver.setPrefix("/");
//        viewResolver.setSuffix(".vm");
//        viewResolver.setContentType("text/html; charset=utf-8");
//        return viewResolver;
//    } 
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if(Config.isDebug) {
            registry.addResourceHandler("/**").addResourceLocations(
                    new File(Consts.ResFolder, "web").toURI().toString());
        } else {
            registry.addResourceHandler("/**").addResourceLocations(
                    "classpath:web/");
        }
    }
    
}
