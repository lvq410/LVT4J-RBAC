package com.lvt4j.rbac.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Role;
import com.lvt4j.rbac.po.User;
import com.lvt4j.rbac.vo.ParamVo;
import com.lvt4j.rbac.mybatis.PlusMapper;

/**
 *
 * @author LV on 2020年8月5日
 */
@Mapper
public interface UserMapper extends PlusMapper<User>,SequenceSetter {

    @Update("update user set id=#{user.id}, name=#{user.name}, des=#{user.des} where autoId=#{user.autoId} ")
    public void set(@Param("user") User user);
    
    @Update("update user set seq=#{seq} where autoId=#{autoId} ")
    public void setSeq(
            @Param("seq") int seq,
            @Param("autoId") int autoId);
    
    @Select("select P.*,UP.val from param P left join user_param UP on P.autoId=UP.paramAutoId and UP.userAutoId=#{autoId} where P.proAutoId=#{proAutoId} order by P.seq")
    public List<ParamVo> params(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Select("select R.* from user_role UR inner join role R on UR.roleAutoId=R.autoId where UR.userAutoId=#{autoId} and UR.proAutoId=#{proAutoId} order by UR.seq")
    public List<Role> roles(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Select("select A.* from user_access UA inner join access A on UA.accessAutoId=A.autoId where UA.userAutoId=#{autoId} and UA.proAutoId=#{proAutoId} order by UA.seq")
    public List<Access> accesses(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Select("select P.* from user_permission UP inner join permission P on UP.permissionAutoId=P.autoId where UP.userAutoId=#{autoId} and UP.proAutoId=#{proAutoId} order by UP.seq")
    public List<Permission> permissions(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    
    @Select("select P.key,UP.val from user_param UP inner join param P on UP.paramAutoId=P.autoId and UP.userAutoId=#{autoId} where P.proAutoId=#{proAutoId}")
    public List<ParamVo> paramKVs(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Select("select R.autoId,R.id from user_role UR inner join role R on UR.roleAutoId=R.autoId where UR.userAutoId=#{autoId} and UR.proAutoId=#{proAutoId} order by UR.seq")
    public List<Role> roleIdAutoIds(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Select("select A.pattern from user_access UA inner join access A on UA.accessAutoId=A.autoId where UA.userAutoId=#{autoId} and UA.proAutoId=#{proAutoId} order by UA.seq")
    public List<String> accessPatterns(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Select("select P.id from user_permission UP inner join permission P on UP.permissionAutoId=P.autoId where UP.userAutoId=#{autoId} and UP.proAutoId=#{proAutoId} order by UP.seq")
    public List<String> permissionIds(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    
    @Delete("delete from user_param where userAutoId=#{autoId} and proAutoId=#{proAutoId}")
    public void cleanParam(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Delete("delete from user_role where userAutoId=#{autoId} and proAutoId=#{proAutoId}")
    public void cleanRole(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Delete("delete from user_access where userAutoId=#{autoId} and proAutoId=#{proAutoId}")
    public void cleanAccess(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    @Delete("delete from user_permission where userAutoId=#{autoId} and proAutoId=#{proAutoId}")
    public void cleanPermission(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId);
    
    @Insert("insert into user_param(userAutoId,proAutoId,paramAutoId,val) values(#{autoId},#{proAutoId},#{paramAutoId},#{val})")
    public void param(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId,
            @Param("paramAutoId") int paramAutoId,
            @Param("val") String val);
    @Insert("insert into user_role(userAutoId,proAutoId,roleAutoId,seq) values(#{autoId},#{proAutoId},#{roleAutoId},#{seq})")
    public void role(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId,
            @Param("roleAutoId") int roleAutoId,
            @Param("seq") int seq);
    @Insert("insert into user_access(userAutoId,proAutoId,accessAutoId,seq) values(#{autoId},#{proAutoId},#{accessAutoId},#{seq})")
    public void access(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId,
            @Param("accessAutoId") int accessAutoId,
            @Param("seq") int seq);
    @Insert("insert into user_permission(userAutoId,proAutoId,permissionAutoId,seq) values(#{autoId},#{proAutoId},#{permissionAutoId},#{seq})")
    public void permission(
            @Param("autoId") int autoId,
            @Param("proAutoId") int proAutoId,
            @Param("permissionAutoId") int permissionAutoId,
            @Param("seq") int seq);
    
    @Delete("delete from user_param where proAutoId=#{proAutoId}")
    public void onProductDelete_param(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from user_role where proAutoId=#{proAutoId}")
    public void onProductDelete_role(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from user_access where proAutoId=#{proAutoId}")
    public void onProductDelete_access(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from user_permission where proAutoId=#{proAutoId}")
    public void onProductDelete_permission(
            @Param("proAutoId") int proAutoId);
    
    
    @Delete("delete from user_param where userAutoId=#{autoId}")
    public void onUserDelete_param(
            @Param("autoId") int autoId);
    @Delete("delete from user_role where userAutoId=#{autoId}")
    public void onUserDelete_role(
            @Param("autoId") int autoId);
    @Delete("delete from user_access where userAutoId=#{autoId}")
    public void onUserDelete_access(
            @Param("autoId") int autoId);
    @Delete("delete from user_permission where userAutoId=#{autoId}")
    public void onUserDelete_permission(
            @Param("autoId") int autoId);
    
    @Delete("delete from user_param where paramAutoId=#{paramAutoId}")
    public void onParamDelete(
            @Param("paramAutoId") int paramAutoId);
    
    @Delete("delete from user_role where roleAutoId=#{roleAutoId}")
    public void onRoleDelete(
            @Param("roleAutoId") int roleAutoId);
    
    @Delete("delete from user_access where accessAutoId=#{accessAutoId}")
    public void onAccessDelete(
            @Param("accessAutoId") int accessAutoId);
    
    @Delete("delete from user_permission where permissionAutoId=#{permissionAutoId}")
    public void onPermissionDelete(
            @Param("permissionAutoId") int permissionAutoId);
    
}