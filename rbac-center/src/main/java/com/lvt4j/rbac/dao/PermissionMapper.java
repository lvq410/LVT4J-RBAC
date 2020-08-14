package com.lvt4j.rbac.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.lvt4j.rbac.mybatis.PlusMapper;
import com.lvt4j.rbac.po.Permission;

/**
 *
 * @author LV on 2020年8月6日
 */
@Mapper
public interface PermissionMapper extends PlusMapper<Permission>,SequenceSetter {

    @Update("update permission set id=#{permission.id}, name=#{permission.name}, des=#{permission.des} where autoId=#{permission.autoId} ")
    public void set(@Param("permission") Permission permission);
    
    @Update("update permission set seq=#{seq} where autoId=#{autoId} ")
    public void setSeq(
            @Param("seq") int seq,
            @Param("autoId") int autoId);
    
    @Delete("delete from permission where proAutoId=#{proAutoId}")
    public void onProductDelete(
            @Param("proAutoId") int proAutoId);
    
}
