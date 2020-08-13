package com.lvt4j.rbac.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否主节点
 * @author LV on 2020年7月10日
 */
public class IsMaster implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if(new DbIsSqlite().matches(context, metadata)) return true;
        return context.getEnvironment().getProperty("server.master", boolean.class);
    }

}