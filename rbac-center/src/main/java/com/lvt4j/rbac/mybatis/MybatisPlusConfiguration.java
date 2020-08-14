package com.lvt4j.rbac.mybatis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;

/**
 *
 * @author LV on 2020年8月5日
 */
@Configuration
public class MybatisPlusConfiguration {

    /** mybatis-plus物理分页必须开启这个 */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
    
}
