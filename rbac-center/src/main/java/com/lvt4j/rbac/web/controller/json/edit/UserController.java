package com.lvt4j.rbac.web.controller.json.edit;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.User;
import com.lvt4j.rbac.vo.UserVo;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月5日
 */
@RestController
@RequestMapping("edit/user")
class UserController extends AbstractEditController {
    private static BeanCopier copier = BeanCopier.create(User.class, UserVo.class, false);
    
    @Autowired
    private UserMapper mapper;
    
    @Read
    @RequestMapping("list")
    public JsonResult list(
            User.Query query,
            @RequestParam(required=false) boolean needAuth,
            @RequestParam(required=false) Integer proAutoId) {
        if(!needAuth) return list(mapper, query);
        Pair<Long, List<User>> pair = mapper.list(query);
        Checker.isTrue(proAutoId!=null, "需要权限信息，但产品未指定");
        List<UserVo> vos = pair.getRight().stream().map(u->toVo(u, proAutoId)).collect(toList());
        return JsonResult.success()
                .dataPut("count", pair.getLeft())
                .dataPut("models", vos);
    }
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            User.Query query,
            @RequestParam(required=false) boolean needAuth,
            @RequestParam(required=false) Integer proAutoId) {
        Checker.isTrue(query.autoId!=null || StringUtils.isNotBlank(query.id), "查询条件id或autoId至少需要一个");
        User user = mapper.selectOne(query.toWrapper());
        if(user==null || !needAuth) return JsonResult.success(user);
        Checker.isTrue(proAutoId!=null, "需要权限信息，但产品未指定");
        return JsonResult.success(toVo(user, proAutoId));
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @Valid User user) {
        user.id = user.id.trim();
        user.name = user.name.trim();
        int duplicated = mapper.selectCount(User.Query.builder().autoIdNot(user.autoId).id(user.id).build().toWrapperWithoutSort());
        Checker.isTrue(duplicated==0, CONFLICT, "用户ID[%s]冲突", user.id);
        
        User orig = null;
        OpLog opLog = OpLog.create(null);
        if(user.autoId==null){
            opLog.action = "新增用户";
            mapper.insert(user);
            user.seq = user.autoId;
            mapper.setSeq(user.seq, user.autoId);
        }else{
            orig = mapper.selectById(user.autoId);
            Checker.isTrue(orig!=null, "原用户[autoId=%]不存在", orig.autoId);
            if(orig.equals(user)) return JsonResult.success(user);
            opLog.action = "修改用户";
            opLog.orig(orig);
            mapper.set(user);
        }
        opLog.now(user);
        oplog(opLog);
        
        //数据变更通知
        String origId = Optional.ofNullable(orig).map(User::getId).orElse(null);
        if(!user.id.equals(origId)){
            if(origId!=null) onDataChange(null, origId, null);
            onDataChange(null, user.id, null);
        }
        
        return JsonResult.success(user);
    }
    
    @Write
    @Transactional
    @RequestMapping("sort")
    public JsonResult sort(
            @RequestParam ListInt autoIds) {
        return sort(autoIds, mapper, "用户");
    }
    
    @Write
    @Transactional
    @RequestMapping("del")
    public JsonResult del(@RequestParam int autoId) {
        User orig = mapper.selectById(autoId);
        if(orig==null) return JsonResult.success();
        
        OpLog opLog = OpLog.create(null);
        opLog.action = "删除用户";
        opLog.orig(orig);
        
        mapper.deleteById(autoId);
        //联动删除
        mapper.onUserDelete_param(autoId);
        mapper.onUserDelete_role(autoId);
        mapper.onUserDelete_access(autoId);
        mapper.onUserDelete_permission(autoId);
        
        oplog(opLog);
        
        onDataChange(null, orig.id, null);
        
        return JsonResult.success();
    }
    
    protected UserVo toVo(User user, int proAutoId) {
        UserVo vo = new UserVo();
        copier.copy(user, vo, null);
        vo.params = mapper.params(user.autoId, proAutoId);
        vo.roles = mapper.roles(user.autoId, proAutoId);
        vo.accesses = mapper.accesses(user.autoId, proAutoId);
        vo.permissions = mapper.permissions(user.autoId, proAutoId);
        return vo;
    }
    
}
