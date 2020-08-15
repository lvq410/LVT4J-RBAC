package com.lvt4j.rbac.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.lvt4j.rbac.mybatis.PlusMapper;

/**
 *
 * @author LV on 2020年8月6日
 */
@Mapper
public interface ParamMapper extends PlusMapper<com.lvt4j.rbac.po.Param>,SequenceSetter {

    @Update("update param set `key`=#{param.key}, name=#{param.name}, des=#{param.des} where autoId=#{param.autoId} ")
    public void set(@Param("param") com.lvt4j.rbac.po.Param param);
    
    @Update("update param set seq=#{seq} where autoId=#{autoId} ")
    public void setSeq(
            @Param("seq") int seq,
            @Param("autoId") int autoId);
    
    @Delete("delete from param where proAutoId=#{proAutoId}")
    public void onProductDelete(
            @Param("proAutoId") int proAutoId);
    
}
