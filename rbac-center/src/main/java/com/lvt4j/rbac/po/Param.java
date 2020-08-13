package com.lvt4j.rbac.po;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.hibernate.validator.constraints.Length;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lvt4j.rbac.dto.Pager;
import com.lvt4j.rbac.mybatis.MybatisPlusQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 配置项
 * @author LV
 */
@Data
@TableName("param")
@EqualsAndHashCode(exclude="seq")
public class Param implements Entity{
    
    @TableId(type=IdType.AUTO)
    public Integer autoId;
    
    @NotNull(message="所属产品不能为空")
    @Positive(message="所属产品异常")
    public Integer proAutoId;
    
    @NotBlank(message="key不能为空")
    @Length(max=200,message="key过长")
    public String key;
    
    @NotBlank(message="名称不能为空")
    @Length(max=200,message="名称过长")
    public String name;
    
    public String des;
    
    public Integer seq;
    
    @Data@Builder
    @NoArgsConstructor@AllArgsConstructor
    public static class Query implements MybatisPlusQuery<Param> {
        public Integer proAutoId;
        public Integer autoId,autoIdNot;
        public String key;
        public String keyword;
        public Pager pager;
        
        @Override
        public QueryWrapper<Param> toWrapperWithoutSort() {
            QueryWrapper<Param> wrapper = Wrappers.query();
            eqWrapper(wrapper, "proAutoId", proAutoId);
            neWrapper(wrapper, "autoId", autoIdNot);
            eqWrapper(wrapper, "autoId", autoId);
            eqWrapper(wrapper, "key", key);
            multiLikeWrapper(wrapper, keyword, "key", "name");
            return wrapper;
        }
        
        @Override
        public QueryWrapper<Param> toWrapper() {
            QueryWrapper<Param> wrapper = toWrapperWithoutSort();
            wrapper.orderByAsc("seq");
            return wrapper;
        }
        
    }
}