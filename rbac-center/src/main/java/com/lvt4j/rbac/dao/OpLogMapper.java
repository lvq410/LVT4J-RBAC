package com.lvt4j.rbac.dao;

import org.apache.ibatis.annotations.Mapper;

import com.lvt4j.rbac.mybatis.PlusMapper;
import com.lvt4j.rbac.po.OpLog;

/**
 *
 * @author LV on 2020年8月5日
 */
@Mapper
public interface OpLogMapper extends PlusMapper<OpLog> {

}