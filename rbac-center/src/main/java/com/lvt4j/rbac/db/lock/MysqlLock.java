package com.lvt4j.rbac.db.lock;

import org.springframework.context.annotation.Conditional;

import com.lvt4j.rbac.condition.DbIsMysql;
import org.springframework.stereotype.Component;

/**
 *
 * @author LV on 2020年8月5日
 */
@Component
@Conditional(DbIsMysql.class)
class MysqlLock implements DbLock {}