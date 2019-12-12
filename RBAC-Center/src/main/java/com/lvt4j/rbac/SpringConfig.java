package com.lvt4j.rbac;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.lvt4j.rbac.web.AdminInterceptor;
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

    @Value("${db-file}")
    private String dbFile;
    
    @Bean
    public SQLiteDataSource dataSource() throws Throwable {
        File db = new File(dbFile);
        if(!db.exists()) initDbFile(db);
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:"+dbFile);
        dataSource.setEnforceForeignKeys(true);
        dataSource.setIncrementalVacuum(1000);
        dataSource.setCacheSize(-200000);
        return dataSource;
    }
    private void initDbFile(File db) throws Exception {
        InputStream is = ClassLoader.getSystemResourceAsStream("rbac.db");
        FileUtils.copyInputStreamToFile(is, db);
    }
    
    @Bean
    public TDB db(@Autowired DataSource dataSource) {
        return new TDB(dataSource);
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
        registry.addInterceptor(dbInterceptor()).addPathPatterns("/**");
    }
    
    @Bean
    public AdminInterceptor authInterceptor() {
        return new AdminInterceptor();
    }
    
    @Bean
    public DBInterceptor dbInterceptor() {
        return new DBInterceptor();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
                Consts.WebFolder.toURI().toString());
    }
    
}