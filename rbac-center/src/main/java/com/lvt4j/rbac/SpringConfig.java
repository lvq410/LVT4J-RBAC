package com.lvt4j.rbac;

import static com.lvt4j.rbac.Consts.WebFolder;
import static com.lvt4j.rbac.Utils.namedThreadFactory;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt4j.rbac.cluster.hazelcast.Discovery;
import com.lvt4j.rbac.condition.DbIsClusterable;
import com.lvt4j.rbac.dto.NodeInfo;

/**
 * Spring基本配置
 * @author LV
 */
@Configuration
public class SpringConfig implements WebMvcConfigurer, AsyncConfigurer, SchedulingConfigurer {

    @Value("${localIp}")
    private String host;
    @Value("${server.port}")
    private int port;
    @Value("${hazelcast.port}")
    private int hazelcastPort;
    @Value("${db.h2.tcp.port}")
    private int h2TcpPort;
    
    @Value("${db.type}")
    private String dbType;
    
    @Value("${hazelcast.discover.mode}")
    private String hazelcastDiscoverMode;
    
    private ThreadPoolExecutor asyncExecutor;
    private ScheduledExecutorService scheduleExecutor;
    
    @PostConstruct
    private void init() {
        if(DbIsClusterable.isClusterableDb(dbType) && !Discovery.isValidMode(hazelcastDiscoverMode)){
            throw new IllegalArgumentException(String.format("数据库类型[%s]为支持分布式类型，请配置合法的hazelcast.discover.mode(rancher or seeds but [%s])", dbType, hazelcastDiscoverMode));
        }else if(!DbIsClusterable.isClusterableDb(dbType) && Discovery.isValidMode(hazelcastDiscoverMode)){
            throw new IllegalArgumentException(String.format("数据库类型[%s]不支持分布式，请配置hazelcast.discover.mode为none", dbType, hazelcastDiscoverMode));
        }
    }
    
    @Bean("localNodeInfo")
    public NodeInfo localNodeInfo() {
        return new NodeInfo(host, port, hazelcastPort, h2TcpPort, ManagementFactory.getRuntimeMXBean().getStartTime());
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        return objectMapper;
    }
    
    @Bean
    public Validator validator() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");
        return localValidatorFactoryBean;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(WebFolder.toURI().toString());
    }
    
    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor = threadPoolExecutor("AsyncExecutor", Runtime.getRuntime().availableProcessors(),
                new LinkedBlockingQueue<Runnable>(), new CallerRunsPolicy());
    }
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        scheduleExecutor = Executors.newScheduledThreadPool(1, namedThreadFactory("ScheduleExecutor"));
        taskRegistrar.setScheduler(scheduleExecutor);
    }
    
    @PreDestroy
    private void destory() throws Throwable {
        if(asyncExecutor!=null){
            asyncExecutor.shutdown();
            asyncExecutor.awaitTermination(10, TimeUnit.SECONDS);
        }
        if(scheduleExecutor!=null){
            scheduleExecutor.shutdown();
            scheduleExecutor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
    
    private static ThreadPoolExecutor threadPoolExecutor(String name, int maximumPoolSize,
            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(maximumPoolSize, maximumPoolSize,
                1, MINUTES, workQueue, namedThreadFactory(name), handler);
        threadPool.allowCoreThreadTimeOut(true);
        return threadPool;
    }
    
}