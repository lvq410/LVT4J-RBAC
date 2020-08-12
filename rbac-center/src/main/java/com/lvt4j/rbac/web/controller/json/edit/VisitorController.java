package com.lvt4j.rbac.web.controller.json.edit;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.dto.MapIntStr;
import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Product;
import com.lvt4j.rbac.po.Role;
import com.lvt4j.rbac.vo.ParamVo;
import com.lvt4j.rbac.web.controller.json.Checker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author LV on 2020年8月6日
 */
@RestController
@RequestMapping("edit/auth/visitor")
public class VisitorController extends AbstractEditController {
    
    @Autowired
    private VisitorMapper mapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            @RequestParam int proAutoId){
        return JsonResult.success(auth(proAutoId));
    }
    
    private VisitorAuth auth(int proAutoId) {
        return new VisitorAuth(mapper.params(proAutoId),
            mapper.roles(proAutoId),
            mapper.accesses(proAutoId),
            mapper.permissions(proAutoId));
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult authVisitorSet(
            @RequestParam int proAutoId,
            @RequestParam(value="params",defaultValue="{}") MapIntStr params,
            @RequestParam(defaultValue="[]") ListInt roleAutoIds,
            @RequestParam(defaultValue="[]") ListInt accessAutoIds,
            @RequestParam(defaultValue="[]") ListInt permissionAutoIds){
        Product product = productMapper.selectById(proAutoId);
        Checker.isTrue(product!=null, "产品[autoId=%s]不存在", proAutoId);
    
        OpLog opLog = OpLog.create(proAutoId);
        opLog.action = "游客授权";
        VisitorAuth orig = auth(proAutoId);
        opLog.orig(orig);
        
        mapper.cleanParam(proAutoId);
        params.forEach((paramAutoId,val)->{
            if(StringUtils.isBlank(val)) return;
            mapper.param(proAutoId, paramAutoId, val.trim());
        });
        mapper.cleanRole(proAutoId);
        IntStream.range(0, roleAutoIds.size()).forEach(i->mapper.role(proAutoId, roleAutoIds.get(i), i));
        mapper.cleanAccess(proAutoId);
        IntStream.range(0, accessAutoIds.size()).forEach(i->mapper.access(proAutoId, accessAutoIds.get(i), i));
        mapper.cleanPermission(proAutoId);
        IntStream.range(0, permissionAutoIds.size()).forEach(i->mapper.permission(proAutoId, permissionAutoIds.get(i), i));
        
        VisitorAuth now = auth(proAutoId);
        if(orig.equals(now)) return JsonResult.success();
        
        opLog.now(now);
        oplog(opLog);
        
        //数据变更通知
        onDataChange(proAutoId);
        
        return JsonResult.success();
    }
    
    @Read
    @RequestMapping("cal")
    public JsonResult cal(
            @RequestParam(defaultValue="[]") ListInt roleAutoIds,
            @RequestParam(defaultValue="[]") ListInt accessAutoIds,
            @RequestParam(defaultValue="[]") ListInt permissionAutoIds) {
        return new AuthCal(false, null, roleAutoIds, accessAutoIds, permissionAutoIds).run();
    }
    
    @Data
    @NoArgsConstructor@AllArgsConstructor
    class VisitorAuth{
        public List<ParamVo> params;
        public List<Role> roles;
        public List<Access> accesses;
        public List<Permission> permissions;
    }
    
}