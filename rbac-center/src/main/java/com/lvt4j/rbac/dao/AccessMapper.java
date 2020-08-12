package com.lvt4j.rbac.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.lvt4j.rbac.mybatis.PlusMapper;
import com.lvt4j.rbac.po.Access;

/**
 *
 * @author LV on 2020年8月6日
 */
@Mapper
public interface AccessMapper extends PlusMapper<Access>,SequenceSetter {

    @Update("update access set pattern=#{access.pattern}, name=#{access.name}, des=#{access.des} where autoId=#{access.autoId} ")
    public void set(@Param("access") Access access);
    
    @Update("update access set seq=#{seq} where autoId=#{autoId} ")
    public void setSeq(
            @Param("seq") int seq,
            @Param("autoId") int autoId);
    
    @Delete("delete from access where proAutoId=#{proAutoId}")
    public void onProductDelete(
            @Param("proAutoId") int proAutoId);
    
}
