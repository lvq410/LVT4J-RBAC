package com.lvt4j.rbac.velocity;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author LV on 2020年8月5日
 */
@Configuration
public class SpringVelocityConfiguration {

    @Bean
    public VelocityConfigurer velocityConfigurer() {
        VelocityConfigurer velocityConfigurer = new VelocityConfigurer();
        Map<String, Object> props = new HashMap<>();
        props.put("input.encoding", "utf-8");
        props.put("output.encoding", "utf-8");
        props.put("check-template-location", true);
        props.put("resource.loader", "custom");
        props.put("custom.resource.loader.class", VelocityTplLoader.class.getName());
        velocityConfigurer.setVelocityPropertiesMap(props);
        return velocityConfigurer;
    }
    @Bean
    public VelocityViewResolver viewResolver() {
        VelocityViewResolver viewResolver = new VelocityViewResolver();
        viewResolver.setPrefix("");
        viewResolver.setSuffix(".vm");
        viewResolver.setCache(true);
        viewResolver.setContentType("text/html;charset=UTF-8");
        viewResolver.setRequestContextAttribute("rc");
        viewResolver.setExposeRequestAttributes(true);
        viewResolver.setExposeSessionAttributes(true);
        viewResolver.setExposeSpringMacroHelpers(true);
        return viewResolver;
    }
    
}