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
 * 角色
 * @author LV
 */
@Data
@TableName("role")
@EqualsAndHashCode(exclude="seq")
public class Role implements Entity{

    @TableId(type=IdType.AUTO)
    public Integer autoId;
    
    @NotNull(message="所属产品不能为空")
    @Positive(message="所属产品异常")
    public Integer proAutoId;
    
    @NotBlank(message="id不能为空")
    @Length(max=200,message="id过长")
    public String id;
    
    @NotBlank(message="名称不能为空")
    @Length(max=200,message="名称过长")
    public String name;
    
    public String des;
    
    public Integer seq;
    
    @Data@Builder
    @NoArgsConstructor@AllArgsConstructor
    public static class Query implements MybatisPlusQuery<Role> {
        public Integer proAutoId;
        public Integer autoId,autoIdNot;
        public String id;
        public String keyword;
        public Integer accessAutoId;
        public Integer permissionAutoId;
        public Pager pager;
        
        @Override
        public QueryWrapper<Role> toWrapperWithoutSort() {
            QueryWrapper<Role> wrapper = Wrappers.query();
            eqWrapper(wrapper, "proAutoId", proAutoId);
            eqWrapper(wrapper, "autoId", autoId);
            neWrapper(wrapper, "autoId", autoIdNot);
            eqWrapper(wrapper, "id", id);
            multiLikeWrapper(wrapper, keyword, "id", "name");
            wrapper.inSql(accessAutoId!=null, "autoId", "select roleAutoId from role_access where accessAutoId="+accessAutoId);
            wrapper.inSql(permissionAutoId!=null, "autoId", "select roleAutoId from role_permission where permissionAutoId="+permissionAutoId);
            return wrapper;
        }
        
        @Override
        public QueryWrapper<Role> toWrapper() {
            QueryWrapper<Role> wrapper = toWrapperWithoutSort();
            wrapper.orderByAsc("seq");
            return wrapper;
        }
        
    }
    
}