package com.lvt4j.rbac.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * @author LV on 2020年8月5日
 */
public class DbIsMysql implements Condition {

    @Override
    public boolean matches(ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        return "mysql".equals(context.getEnvironment().getProperty("db.type"));
    }

}