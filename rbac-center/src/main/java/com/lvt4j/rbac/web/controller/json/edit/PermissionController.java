package com.lvt4j.rbac.web.controller.json.edit;

import static org.springframework.http.HttpStatus.CONFLICT;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.PermissionMapper;
import com.lvt4j.rbac.dao.RoleMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月6日
 */
@RestController
@RequestMapping("edit/permission")
public class PermissionController extends AbstractEditController {

    @Autowired
    private PermissionMapper mapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Read
    @RequestMapping("list")
    public JsonResult list(
            Permission.Query query) {
        return list(mapper, query);
    }
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            Permission.Query query) {
        Checker.isTrue(query.autoId!=null || StringUtils.isNotBlank(query.id), "查询条件id或autoId至少需要一个");
        return get(mapper, query);
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @Valid Permission permission){
        permission.id = permission.id.trim();
        permission.name = permission.name.trim();
        int duplicated = mapper.selectCount(Permission.Query.builder().autoIdNot(permission.autoId).proAutoId(permission.proAutoId).id(permission.id).build().toWrapperWithoutSort());
        Checker.isTrue(duplicated==0, CONFLICT, "授权项id[%s]冲突", permission.id);
        
        Permission orig = null;
        OpLog opLog = OpLog.create(permission.proAutoId);
        if(permission.autoId==null){
            opLog.action = "新增授权项";
            mapper.insert(permission);
            permission.seq = permission.autoId;
            mapper.setSeq(permission.seq, permission.autoId);
        }else{
            orig = mapper.selectById(permission.autoId);
            Checker.isTrue(orig!=null, "原授权项[autoId=%]不存在", permission.autoId);
            if(orig.equals(permission)) return JsonResult.success(permission);
            opLog.action = "修改授权项";
            opLog.orig(orig);
            mapper.set(permission);
        }
        opLog.now(permission);
        oplog(opLog);
        
        //数据变更通知
        if(orig!=null){ //仅修改时需要
            if(!permission.id.equals(orig.id)){ //仅id不同时需要
                onDataChange(permission.proAutoId);
            }
        }
        
        return JsonResult.success(permission);
    }

    @Write
    @Transactional
    @RequestMapping("sort")
    public JsonResult sort(
            @RequestParam ListInt autoIds) {
        return sort(autoIds, mapper, "授权项");
    }
    
    @Write
    @Transactional
    @RequestMapping("del")
    public JsonResult del(@RequestParam int autoId) {
        Permission orig = mapper.selectById(autoId);
        if(orig==null) return JsonResult.success();
        
        OpLog opLog = OpLog.create(autoId);
        opLog.action = "删除授权项";
        opLog.orig(orig);
        
        mapper.deleteById(autoId);
        //联动删除
        roleMapper.onPermissionDelete(autoId);
        
        visitorMapper.onPermissionDelete(autoId);
        
        userMapper.onPermissionDelete(autoId);
        
        oplog(opLog);
        
        onDataChange(orig.proAutoId);
        
        return JsonResult.success();
    }
}
