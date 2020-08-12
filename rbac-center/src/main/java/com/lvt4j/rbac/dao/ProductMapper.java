package com.lvt4j.rbac.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.lvt4j.rbac.mybatis.PlusMapper;
import com.lvt4j.rbac.po.Product;

/**
 *
 * @author LV on 2020年8月5日
 */
@Mapper
public interface ProductMapper extends PlusMapper<Product>,SequenceSetter {

    @Update("update product set id=#{pro.id}, name=#{pro.name}, des=#{pro.des} where autoId=#{pro.autoId} ")
    public void set(@Param("pro") Product pro);

    @Update("update product set lastModify=#{lastModify} where id=#{id} ")
    public void setLastModify(
            @Param("lastModify") long lastModify,
            @Param("id") String id);
    
    @Update("update product set lastModify=#{lastModify}")
    public void setAllLastModify(
            @Param("lastModify") long lastModify);
    
    @Update("update product set seq=#{seq} where autoId=#{autoId} ")
    public void setSeq(
            @Param("seq") int seq,
            @Param("autoId") int autoId);
    
}