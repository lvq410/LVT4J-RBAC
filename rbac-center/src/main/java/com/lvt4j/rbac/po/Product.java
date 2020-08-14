package com.lvt4j.rbac.po;

import javax.validation.constraints.NotBlank;

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
 * 产品
 * @author LV
 */
@Data
@TableName("product")
@EqualsAndHashCode(exclude={"lastModify","seq"})
public class Product implements Entity{
    
    @TableId(type=IdType.AUTO)
    public Integer autoId;
    
    @NotBlank(message="ID不能为空")
    @Length(max=200,message="ID过长")
    public String id;
    
    @NotBlank(message="名称不能为空")
    @Length(max=200,message="名称过长")
    public String name;
    
    public String des;
    
    public long lastModify;
    
    public Integer seq;
    
    @Data@Builder
    @NoArgsConstructor@AllArgsConstructor
    public static class Query implements MybatisPlusQuery<Product> {
        public Integer autoId,autoIdNot;
        public String id;
        public String keyword;
        public Pager pager;
        
        @Override
        public QueryWrapper<Product> toWrapperWithoutSort() {
            QueryWrapper<Product> wrapper = Wrappers.query();
            eqWrapper(wrapper, "autoId", autoId);
            neWrapper(wrapper, "autoId", autoIdNot);
            eqWrapper(wrapper, "id", id);
            multiLikeWrapper(wrapper, keyword, "id", "name");
            return wrapper;
        }
        
        @Override
        public QueryWrapper<Product> toWrapper() {
            QueryWrapper<Product> wrapper = toWrapperWithoutSort();
            wrapper.orderByAsc("seq");
            return wrapper;
        }
        
    }
}
