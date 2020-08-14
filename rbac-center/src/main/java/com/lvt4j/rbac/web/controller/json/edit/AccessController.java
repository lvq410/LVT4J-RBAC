package com.lvt4j.rbac.web.controller.json.edit;

import static org.springframework.http.HttpStatus.CONFLICT;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.AccessMapper;
import com.lvt4j.rbac.dao.RoleMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月6日
 */
@RestController
@RequestMapping("edit/access")
public class AccessController extends AbstractEditController {

    @Autowired
    private AccessMapper mapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Read
    @RequestMapping("list")
    public JsonResult list(
            Access.Query query) {
        return list(mapper, query);
    }
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            Access.Query query) {
        Checker.isTrue(query.autoId!=null || StringUtils.isNotBlank(query.pattern), "查询条件pattern或autoId至少需要一个");
        return get(mapper, query);
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @Valid Access access){
        access.pattern = access.pattern.trim();
        access.name = access.name.trim();
        int duplicated = mapper.selectCount(Access.Query.builder().autoIdNot(access.autoId).proAutoId(access.proAutoId).pattern(access.pattern).build().toWrapperWithoutSort());
        Checker.isTrue(duplicated==0, CONFLICT, "访问项pattern[%s]冲突", access.pattern);
        
        Access orig = null;
        OpLog opLog = OpLog.create(access.proAutoId);
        if(access.autoId==null){
            opLog.action = "新增访问项";
            mapper.insert(access);
            access.seq = access.autoId;
            mapper.setSeq(access.seq, access.autoId);
        }else{
            orig = mapper.selectById(access.autoId);
            Checker.isTrue(orig!=null, "原访问项[autoId=%]不存在", access.autoId);
            if(orig.equals(access)) return JsonResult.success(access);
            opLog.action = "修改访问项";
            opLog.orig(orig);
            mapper.set(access);
        }
        opLog.now(access);
        oplog(opLog);
        
        //数据变更通知
        if(orig!=null){ //仅修改时需要
            if(!access.pattern.equals(orig.pattern)){ //仅pattern不同时需要
                onDataChange(access.proAutoId);
            }
        }
        
        return JsonResult.success(access);
    }
    
    @Write
    @Transactional
    @RequestMapping("sort")
    public JsonResult sort(
            @RequestParam ListInt autoIds) {
        return sort(autoIds, mapper, "访问项");
    }
    
    @Write
    @Transactional
    @RequestMapping("del")
    public JsonResult del(@RequestParam int autoId) {
        Access orig = mapper.selectById(autoId);
        if(orig==null) return JsonResult.success();
        
        OpLog opLog = OpLog.create(autoId);
        opLog.action = "删除访问项";
        opLog.orig(orig);
        
        mapper.deleteById(autoId);
        //联动删除
        roleMapper.onAccessDelete(autoId);
        
        visitorMapper.onAccessDelete(autoId);
        
        userMapper.onAccessDelete(autoId);
        
        oplog(opLog);
        
        onDataChange(orig.proAutoId);
        
        return JsonResult.success();
    }
    
}
