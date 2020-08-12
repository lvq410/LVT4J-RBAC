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
 * 用户
 * @author LV
 */
@Data
@TableName("user")
@EqualsAndHashCode(exclude={"seq"})
public class User implements Entity{
    
    @TableId(type=IdType.AUTO)
    public Integer autoId;
    
    @NotBlank(message="用户ID不能为空")
    @Length(max=200,message="id过长")
    public String id;
    
    @NotBlank(message="用户名称不能为空")
    @Length(max=200,message="名称过长")
    public String name;
    
    public String des;
    
    public Integer seq;

    @Data@Builder
    @NoArgsConstructor@AllArgsConstructor
    public static class Query implements MybatisPlusQuery<User> {
        public Integer autoId,autoIdNot;
        public String id;
        public String keyword;
        public Integer roleAutoId;
        public Integer accessAutoId;
        public Integer permissionAutoId;
        public Pager pager;
        
        @Override
        public QueryWrapper<User> toWrapperWithoutSort() {
            QueryWrapper<User> wrapper = Wrappers.query();
            eqWrapper(wrapper, "autoId", autoId);
            neWrapper(wrapper, "autoId", autoIdNot);
            eqWrapper(wrapper, "id", id);
            multiLikeWrapper(wrapper, keyword, "id", "name");
            wrapper.inSql(roleAutoId!=null, "autoId", "select userAutoId from user_role where roleAutoId="+roleAutoId);
            wrapper.inSql(accessAutoId!=null, "autoId", "select userAutoId from user_access where accessAutoId="+accessAutoId);
            wrapper.inSql(permissionAutoId!=null, "autoId", "select userAutoId from user_permission where permissionAutoId="+permissionAutoId);
            return wrapper;
        }
        
        @Override
        public QueryWrapper<User> toWrapper() {
            QueryWrapper<User> wrapper = toWrapperWithoutSort();
            wrapper.orderByAsc("seq");
            return wrapper;
        }
    }
    
}