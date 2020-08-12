package com.lvt4j.rbac.db.lock;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.lvt4j.rbac.condition.DbIsH2;

/**
 *
 * @author lichenxi on 2020年7月24日
 */
@Component
@Conditional(DbIsH2.class)
class H2Lock implements DbLock {}