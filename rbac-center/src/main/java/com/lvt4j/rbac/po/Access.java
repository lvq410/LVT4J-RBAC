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
 * 访问项
 * @author LV
 */
@Data
@TableName("access")
@EqualsAndHashCode(exclude="seq")
public class Access implements Entity{
    
    @TableId(type=IdType.AUTO)
    public Integer autoId;
    
    @NotNull(message="所属产品不能为空")
    @Positive(message="所属产品异常")
    public Integer proAutoId;
    
    @NotBlank(message="pattern不能为空")
    @Length(max=200,message="pattern过长")
    public String pattern;
    
    @NotBlank(message="名称不能为空")
    @Length(max=200,message="名称过长")
    public String name;
    
    public String des;
    
    public Integer seq;
    
    @Data@Builder
    @NoArgsConstructor@AllArgsConstructor
    public static class Query implements MybatisPlusQuery<Access> {
        public Integer proAutoId;
        public Integer autoId,autoIdNot;
        public String pattern;
        public String keyword;
        public Pager pager;
        
        @Override
        public QueryWrapper<Access> toWrapperWithoutSort() {
            QueryWrapper<Access> wrapper = Wrappers.query();
            eqWrapper(wrapper, "proAutoId", proAutoId);
            neWrapper(wrapper, "autoId", autoIdNot);
            eqWrapper(wrapper, "autoId", autoId);
            eqWrapper(wrapper, "pattern", pattern);
            multiLikeWrapper(wrapper, keyword, "pattern", "name");
            return wrapper;
        }
        
        @Override
        public QueryWrapper<Access> toWrapper() {
            QueryWrapper<Access> wrapper = toWrapperWithoutSort();
            wrapper.orderByAsc("seq");
            return wrapper;
        }
        
    }
}