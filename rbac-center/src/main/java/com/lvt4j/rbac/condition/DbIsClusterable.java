package com.lvt4j.rbac.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 数据库类型为可组建集群式
 * 包括h2和mysql
 * @author LV on 2021年3月3日
 */
public class DbIsClusterable implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isClusterableDb(context.getEnvironment().getProperty("db.type"));
    }

    public static boolean isClusterableDb(String dbType) {
        return "h2".equals(dbType) || "mysql".equals(dbType);
    }
    
}