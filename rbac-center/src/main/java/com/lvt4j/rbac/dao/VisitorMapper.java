package com.lvt4j.rbac.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Role;
import com.lvt4j.rbac.vo.ParamVo;

/**
 *
 * @author LV on 2020年8月6日
 */
@Mapper
public interface VisitorMapper {

    @Select("select P.*,VP.val from param P left join visitor_param VP on P.autoId=VP.paramAutoId where P.proAutoId=#{proAutoId} order by P.seq")
    public List<ParamVo> params(
            @Param("proAutoId") int proAutoId);
    @Select("select R.* from visitor_role VR inner join role R on VR.roleAutoId=R.autoId where VR.proAutoId=#{proAutoId} order by VR.seq")
    public List<Role> roles(
            @Param("proAutoId") int proAutoId);
    @Select("select A.* from visitor_access VA inner join access A on VA.accessAutoId=A.autoId where VA.proAutoId=#{proAutoId} order by VA.seq")
    public List<Access> accesses(
            @Param("proAutoId") int proAutoId);
    @Select("select P.* from visitor_permission VP inner join permission P on VP.permissionAutoId=P.autoId where VP.proAutoId=#{proAutoId} order by VP.seq")
    public List<Permission> permissions(
            @Param("proAutoId") int proAutoId);
    
    @Select("select P.key,VP.val from param P left join visitor_param VP on P.autoId=VP.paramAutoId where P.proAutoId=#{proAutoId} order by P.seq")
    public List<ParamVo> paramKVs(
            @Param("proAutoId") int proAutoId);
    @Select("select R.autoId,R.id from visitor_role VR inner join role R on VR.roleAutoId=R.autoId where VR.proAutoId=#{proAutoId} order by VR.seq")
    public List<Role> roleIdAutoIds(
            @Param("proAutoId") int proAutoId);
    @Select("select A.pattern from visitor_access VA inner join access A on VA.accessAutoId=A.autoId where VA.proAutoId=#{proAutoId} order by VA.seq")
    public List<String> accessPatterns(
            @Param("proAutoId") int proAutoId);
    @Select("select P.id from visitor_permission VP inner join permission P on VP.permissionAutoId=P.autoId where VP.proAutoId=#{proAutoId} order by VP.seq")
    public List<String> permissionIds(
            @Param("proAutoId") int proAutoId);
    
    @Select("select roleAutoId from visitor_role where proAutoId=#{proAutoId} order by seq")
    public List<Integer> roleAutoIds(
            @Param("proAutoId") int proAutoId);
    @Select("select accessAutoId from visitor_access where proAutoId=#{proAutoId} order by seq")
    public List<Integer> accessAutoIds(
            @Param("proAutoId") int proAutoId);
    @Select("select permissionAutoId from visitor_permission where proAutoId=#{proAutoId} order by seq")
    public List<Integer> permissionAutoIds(
            @Param("proAutoId") int proAutoId);
    
    @Delete("delete from visitor_param where proAutoId=#{proAutoId}")
    public void cleanParam(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from visitor_role where proAutoId=#{proAutoId}")
    public void cleanRole(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from visitor_access where proAutoId=#{proAutoId}")
    public void cleanAccess(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from visitor_permission where proAutoId=#{proAutoId}")
    public void cleanPermission(
            @Param("proAutoId") int proAutoId);
    
    @Insert("insert into visitor_param(proAutoId,paramAutoId,val) values(#{proAutoId},#{paramAutoId},#{val})")
    public void param(
            @Param("proAutoId") int proAutoId,
            @Param("paramAutoId") int paramAutoId,
            @Param("val") String val);
    @Insert("insert into visitor_role(proAutoId,roleAutoId,seq) values(#{proAutoId},#{roleAutoId},#{seq})")
    public void role(
            @Param("proAutoId") int proAutoId,
            @Param("roleAutoId") int roleAutoId,
            @Param("seq") int seq);
    @Insert("insert into visitor_access(proAutoId,accessAutoId,seq) values(#{proAutoId},#{accessAutoId},#{seq})")
    public void access(
            @Param("proAutoId") int proAutoId,
            @Param("accessAutoId") int accessAutoId,
            @Param("seq") int seq);
    @Insert("insert into visitor_permission(proAutoId,permissionAutoId,seq) values(#{proAutoId},#{permissionAutoId},#{seq})")
    public void permission(
            @Param("proAutoId") int proAutoId,
            @Param("permissionAutoId") int permissionAutoId,
            @Param("seq") int seq);
    
    @Delete("delete from visitor_param where proAutoId=#{proAutoId}")
    public void onProductDelete_param(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from visitor_role where proAutoId=#{proAutoId}")
    public void onProductDelete_role(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from visitor_access where proAutoId=#{proAutoId}")
    public void onProductDelete_access(
            @Param("proAutoId") int proAutoId);
    @Delete("delete from visitor_permission where proAutoId=#{proAutoId}")
    public void onProductDelete_permission(
            @Param("proAutoId") int proAutoId);
    
    
    @Delete("delete from visitor_param where paramAutoId=#{paramAutoId}")
    public void onParamDelete(
            @Param("paramAutoId") int paramAutoId);
    
    @Delete("delete from visitor_role where roleAutoId=#{roleAutoId}")
    public void onRoleDelete(
            @Param("roleAutoId") int roleAutoId);
    
    @Delete("delete from visitor_access where accessAutoId=#{accessAutoId}")
    public void onAccessDelete(
            @Param("accessAutoId") int accessAutoId);
    
    @Delete("delete from visitor_permission where permissionAutoId=#{permissionAutoId}")
    public void onPermissionDelete(
            @Param("permissionAutoId") int permissionAutoId);
}
