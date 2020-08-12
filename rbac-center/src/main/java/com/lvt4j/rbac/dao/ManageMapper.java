package com.lvt4j.rbac.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
//import org.springframework.context.annotation.Conditional;
//
//import com.lvt4j.rbac.condition.DbIsH2;
//import com.lvt4j.rbac.condition.IsMaster;

/**
 *
 * @author LV on 2020年8月5日
 */
@Mapper
//@Conditional({DbIsH2.class, IsMaster.class})
public interface ManageMapper {

    @Select("backup to #{backup}")
    public void backup(@Param("backup") String backup);
    
}