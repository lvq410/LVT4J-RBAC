package com.lvt4j.rbac.web.controller.json.edit;

import static org.springframework.http.HttpStatus.CONFLICT;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.ParamMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Param;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月6日
 */
@RestController
@RequestMapping("edit/param")
class ParamController extends AbstractEditController {

    @Autowired
    private ParamMapper mapper;
    
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Read
    @RequestMapping("list")
    public JsonResult list(
            Param.Query query) {
        return list(mapper, query);
    }
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            Param.Query query) {
        Checker.isTrue(query.autoId!=null || StringUtils.isNotBlank(query.key), "查询条件key或autoId至少需要一个");
        return get(mapper, query);
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @Valid Param param){
        param.key = param.key.trim();
        param.name = param.name.trim();
        int duplicated = mapper.selectCount(Param.Query.builder().autoIdNot(param.autoId).proAutoId(param.proAutoId).key(param.key).build().toWrapperWithoutSort());
        Checker.isTrue(duplicated==0, CONFLICT, "配置项key[%s]冲突", param.key);
        
        Param orig = null;
        OpLog opLog = OpLog.create(param.proAutoId);
        if(param.autoId==null){
            opLog.action = "新增配置项";
            mapper.insert(param);
            param.seq = param.autoId;
            mapper.setSeq(param.seq, param.autoId);
        }else{
            orig = mapper.selectById(param.autoId);
            Checker.isTrue(orig!=null, "原配置项[autoId=%]不存在", param.autoId);
            if(orig.equals(param)) return JsonResult.success(param);
            opLog.action = "修改配置项";
            opLog.orig(orig);
            mapper.set(param);
        }
        opLog.now(param);
        oplog(opLog);
        
        //数据变更通知
        if(orig!=null){ //仅修改时需要通知
            if(!param.key.equals(orig.key)){ //仅修改后key不同时需要
                onDataChange(param.proAutoId);
            }
        }
        
        return JsonResult.success(param);
    }
    
    @Write
    @Transactional
    @RequestMapping("sort")
    public JsonResult sort(
            @RequestParam ListInt autoIds) {
        return sort(autoIds, mapper, "配置项");
    }
    
    @Write
    @Transactional
    @RequestMapping("del")
    public JsonResult del(@RequestParam int autoId) {
        Param orig = mapper.selectById(autoId);
        if(orig==null) return JsonResult.success();
        
        OpLog opLog = OpLog.create(autoId);
        opLog.action = "删除配置项";
        opLog.orig(orig);
        
        mapper.deleteById(autoId);
        //联动删除
        visitorMapper.onParamDelete(autoId);
        userMapper.onParamDelete(autoId);
        
        oplog(opLog);
        
        onDataChange(orig.proAutoId);
        
        return JsonResult.success();
    }
    
}