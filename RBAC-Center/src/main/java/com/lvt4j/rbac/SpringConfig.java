package com.lvt4j.rbac;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.sqlite.SQLiteDataSource;

import com.lvt4j.basic.TDB;
import com.lvt4j.rbac.web.CurProInterceptor;
import com.lvt4j.rbac.web.DBInterceptor;
import com.lvt4j.spring.ControllerConfig;

/**
 * MVC配置
 * @author LV
 */
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
//        db.openPrintSQL();
        Consts.DB = db;
        return db;
    }
    
    @Bean
    public ControllerConfig controllerConfig() {
        ControllerConfig controllerConfig = new ControllerConfig();
        controllerConfig.setPropertyEditorSupportClses(Consts.SupportHandlerMethodTypes);
        return controllerConfig;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dbInterceptor());
        registry.addInterceptor(curProInterceptor()).addPathPatterns("/edit/**");
    }
    
    @Bean
    public DBInterceptor dbInterceptor() {
        return new DBInterceptor();
    }
    
    @Bean
    public CurProInterceptor curProInterceptor() {
        return new CurProInterceptor();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**").addResourceLocations(
//                "classpath:/web/");
        registry.addResourceHandler("/**").addResourceLocations(
                new File("web").toURI().toString());
    }
    
}
