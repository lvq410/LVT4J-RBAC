package com.lvt4j.rbac.condition;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否从节点
 * @author lichenxi on 2020年7月10日
 */
public class IsSlave extends IsMaster {

    @Override
    public boolean matches(ConditionContext context,
            AnnotatedTypeMetadata metadata) {
        return !super.matches(context, metadata);
    }
    
}