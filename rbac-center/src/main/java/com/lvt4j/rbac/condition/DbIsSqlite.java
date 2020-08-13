package com.lvt4j.rbac.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * @author LV on 2020年7月24日
 */
public class DbIsSqlite implements Condition {

    @Override
    public boolean matches(ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        return "sqlite".equals(context.getEnvironment().getProperty("db.type"));
    }
    
}