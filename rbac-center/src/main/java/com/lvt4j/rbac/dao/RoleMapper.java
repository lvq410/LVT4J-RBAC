package com.lvt4j.rbac.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.lvt4j.rbac.mybatis.PlusMapper;
import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Role;

/**
 *
 * @author LV on 2020年8月6日
 */
@Mapper
public interface RoleMapper extends PlusMapper<Role>,SequenceSetter {

    @Update("update role set id=#{role.id}, name=#{role.name}, des=#{role.des} where autoId=#{role.autoId} ")
    public void set(@Param("role") Role role);
    
    @Update("update role set seq=#{seq} where autoId=#{autoId} ")
    public void setSeq(
            @Param("seq") int seq,
            @Param("autoId") int autoId);
    
    @Select("select A.* from role_access RA inner join access A on RA.accessAutoId=A.autoId where RA.roleAutoId=#{autoId} order by RA.seq")
    public List<Access> accesses(
            @Param("autoId") int autoId);
    @Select("select P.* from role_permission RP inner join permission P on RP.permissionAutoId=P.autoId where RP.roleAutoId=#{autoId} order by RP.seq")
    public List<Permission> permissions(
            @Param("autoId") int autoId);
    
    @Select("select A.pattern from role_access RA inner join access A on RA.accessAutoId=A.autoId where RA.roleAutoId=#{autoId} order by RA.seq")
    public List<String> accessPatterns(
            @Param("autoId") int autoId);
    @Select("select P.id from role_permission RP inner join permission P on RP.permissionAutoId=P.autoId where RP.roleAutoId=#{autoId} order by RP.seq")
    public List<String> permissionIds(
            @Param("autoId") int autoId);
    
    @Select("select accessAutoId from role_access where roleAutoId=#{autoId} order by seq")
    public List<Integer> accessAutoIds(
            @Param("autoId") int autoId);
    @Select("select permissionAutoId from role_permission where roleAutoId=#{autoId} order by seq")
    public List<Integer> permissionAutoIds(
            @Param("autoId") int autoId);
    
    @Delete("delete from role_access where roleAutoId=#{autoId}")
    public void cleanAccess(
            @Param("autoId") int autoId);
    @Delete("delete from role_permission where roleAutoId=#{autoId}")
    public void cleanPermission(
            @Param("autoId") int autoId);
    
    @Insert("insert into role_access(proAutoId,roleAutoId,accessAutoId,seq) values(#{proAutoId},#{roleAutoId},#{accessAutoId},#{seq})")
    public void access(
            @Param("proAutoId") int proAutoId,
            @Param("roleAutoId") int roleAutoId,
            @Param("accessAutoId") int accessAutoId,
            @Param("seq") int seq);
    
    @Insert("insert into role_permission(proAutoId,roleAutoId,permissionAutoId,seq) values(#{proAutoId},#{roleAutoId},#{permissionAutoId},#{seq})")
    public void permission(
            @Param("proAutoId") int proAutoId,
            @Param("roleAutoId") int roleAutoId,
            @Param("permissionAutoId") int permissionAutoId,
            @Param("seq") int seq);
    
    
    @Delete("delete from role where proAutoId=#{proAutoId}")
    public void onProductDelete(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from role_access where proAutoId=#{proAutoId}")
    public void onProductDelete_access(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from role_permission where proAutoId=#{proAutoId}")
    public void onProductDelete_permission(
            @Param("proAutoId") int proAutoId);
    
    @Delete("delete from role_access where roleAutoId=#{roleAutoId}")
    public void onRoleDelete_access(
            @Param("roleAutoId") int roleAutoId);
    @Delete("delete from role_permission where roleAutoId=#{roleAutoId}")
    public void onRoleDelete_permission(
            @Param("roleAutoId") int roleAutoId);
    
    @Delete("delete from role_access where accessAutoId=#{accessAutoId}")
    public void onAccessDelete(
            @Param("accessAutoId") int accessAutoId);
    
    @Delete("delete from role_permission where permissionAutoId=#{permissionAutoId}")
    public void onPermissionDelete(
            @Param("permissionAutoId") int permissionAutoId);
    
}