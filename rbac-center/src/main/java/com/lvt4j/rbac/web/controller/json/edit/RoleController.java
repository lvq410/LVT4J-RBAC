package com.lvt4j.rbac.web.controller.json.edit;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ListUtils.isEqualList;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.AccessMapper;
import com.lvt4j.rbac.dao.PermissionMapper;
import com.lvt4j.rbac.dao.RoleMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Role;
import com.lvt4j.rbac.vo.RoleVo;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月6日
 */
@RestController
@RequestMapping("edit/role")
public class RoleController extends AbstractEditController {
    private static BeanCopier copier = BeanCopier.create(Role.class, RoleVo.class, false);

    @Autowired
    private RoleMapper mapper;
    
    @Autowired
    private AccessMapper accessMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Read
    @RequestMapping("list")
    public JsonResult list(
            Role.Query query,
            @RequestParam(required=false) boolean needAuth) {
        if(!needAuth) list(mapper, query);
        Pair<Long, List<Role>> pair = mapper.list(query);
        List<RoleVo> vos = pair.getRight().stream().map(this::toVo).collect(toList());
        return JsonResult.success()
                .dataPut("count", pair.getLeft())
                .dataPut("models", vos);
    }
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            Role.Query query,
            @RequestParam(required=false) boolean needAuth) {
        Checker.isTrue(query.autoId!=null || StringUtils.isNotBlank(query.id), "查询条件id或autoId至少需要一个");
        Role role = mapper.selectOne(query.toWrapper());
        if(role==null || !needAuth) return JsonResult.success(role);
        return JsonResult.success(toVo(role));
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @Valid Role role,
            @RequestParam(defaultValue="[]") ListInt accessAutoIds,
            @RequestParam(defaultValue="[]") ListInt permissionAutoIds) {
        role.id = role.id.trim();
        role.name = role.name.trim();
        int duplicated = mapper.selectCount(Role.Query.builder().autoIdNot(role.autoId).proAutoId(role.proAutoId).id(role.id).build().toWrapperWithoutSort());
        Checker.isTrue(duplicated==0, CONFLICT, "角色ID[%s]冲突", role.id);
        
        RoleVo now = new RoleVo();
        copier.copy(role, now, null);
        now.accesses = accessAutoIds.stream().map(accessMapper::selectById).filter(Objects::nonNull).collect(toList());
        now.permissions = permissionAutoIds.stream().map(permissionMapper::selectById).filter(Objects::nonNull).collect(toList());
        RoleVo orig = null;
        OpLog opLog = OpLog.create(role.proAutoId);
        if(role.autoId==null){
            opLog.action = "新增角色";
            mapper.insert(role);
            opLog.proAutoId = role.seq = role.autoId;
            mapper.setSeq(role.autoId, role.autoId);
        }else{
            Role origPo = mapper.selectById(role.autoId);
            Checker.isTrue(origPo!=null, "原角色[autoId=%]不存在", role.autoId);
            orig = toVo(origPo);
            if(orig.equals(now)) return JsonResult.success(role);
            opLog.action = "修改角色";
            opLog.orig(orig);
            mapper.set(role);
        }
        mapper.cleanAccess(role.autoId);
        IntStream.range(0, now.accesses.size()).forEach(i->mapper.access(role.proAutoId, role.autoId, now.accesses.get(i).autoId, i));
        mapper.cleanPermission(role.autoId);
        IntStream.range(0, now.permissions.size()).forEach(i->mapper.permission(role.proAutoId, role.autoId, now.permissions.get(i).autoId, i));
        opLog.now(now);
        oplog(opLog);
        
        //数据变更通知
        if(orig!=null){ //仅修改时通知
            List<Access> origAccesses = Optional.ofNullable(orig).map(RoleVo::getAccesses).orElse(null);
            List<Permission> origPermissions = Optional.ofNullable(orig).map(RoleVo::getPermissions).orElse(null);
            if(!now.id.equals(orig.id) || !isEqualList(origAccesses, now.accesses) || !isEqualList(origPermissions, now.permissions)){
                //仅修改后id或权限不同时通知
                onDataChange(now.proAutoId);
            }
        }
        
        return JsonResult.success(role);
    }
    
    @Write
    @Transactional
    @RequestMapping("sort")
    public JsonResult sort(
            @RequestParam ListInt autoIds) {
        return sort(autoIds, mapper, "角色");
    }
    
    @Write
    @Transactional
    @RequestMapping("del")
    public JsonResult del(@RequestParam int autoId) {
        Role orig = mapper.selectById(autoId);
        if(orig==null) return JsonResult.success();
        
        OpLog opLog = OpLog.create(orig.proAutoId);
        opLog.action = "删除角色";
        opLog.orig(toVo(orig));
        
        mapper.deleteById(autoId);
        //联动删除
        mapper.onRoleDelete_access(autoId);
        mapper.onRoleDelete_permission(autoId);
        
        visitorMapper.onRoleDelete(autoId);
        
        userMapper.onRoleDelete(autoId);
        
        oplog(opLog);
        
        onDataChange(orig.proAutoId);
        
        return JsonResult.success();
    }
    
    private RoleVo toVo(Role role) {
        RoleVo vo = new RoleVo();
        copier.copy(role, vo, null);
        vo.accesses = mapper.accesses(role.autoId);
        vo.permissions = mapper.permissions(role.autoId);
        return vo;
    }
    
}