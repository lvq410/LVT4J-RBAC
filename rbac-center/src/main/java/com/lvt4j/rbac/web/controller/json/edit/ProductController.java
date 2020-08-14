package com.lvt4j.rbac.web.controller.json.edit;

import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.AccessMapper;
import com.lvt4j.rbac.dao.ParamMapper;
import com.lvt4j.rbac.dao.PermissionMapper;
import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.dao.RoleMapper;
import com.lvt4j.rbac.dao.UserMapper;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.db.lock.Write;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Product;
import com.lvt4j.rbac.web.controller.json.Checker;

/**
 *
 * @author LV on 2020年8月5日
 */
@RestController
@RequestMapping("edit/product")
class ProductController extends AbstractEditController {

    @Autowired
    private ProductMapper mapper;
    
    @Autowired
    private ParamMapper paramMapper;
    @Autowired
    private AccessMapper accessMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Read
    @RequestMapping("list")
    public JsonResult list(
            Product.Query query) {
        return list(mapper, query);
    }
    
    @Read
    @RequestMapping("get")
    public JsonResult get(
            Product.Query query) {
        Checker.isTrue(query.autoId!=null || StringUtils.isNotBlank(query.id), "查询条件id或autoId至少需要一个");
        return get(mapper, query);
    }
    
    @Write
    @Transactional
    @RequestMapping("set")
    public JsonResult set(
            @Valid Product product) {
        product.id = product.id.trim();
        product.name = product.name.trim();
        int duplicated = mapper.selectCount(Product.Query.builder().autoIdNot(product.autoId).id(product.id).build().toWrapperWithoutSort());
        Checker.isTrue(duplicated==0, CONFLICT, "产品ID[%s]冲突", product.id);
        
        Product orig = null;
        OpLog opLog = OpLog.create(product.autoId);
        if(product.autoId==null){
            opLog.action = "新增产品";
            mapper.insert(product);
            opLog.proAutoId = product.seq = product.autoId;
            mapper.setSeq(product.autoId, product.autoId);
        }else{
            orig = mapper.selectById(product.autoId);
            Checker.isTrue(orig!=null, "原产品[autoId=%]不存在", product.autoId);
            if(orig.equals(product)) return JsonResult.success(product);
            opLog.action = "修改产品";
            opLog.orig(orig);
            mapper.set(product);
        }
        product.lastModify = System.currentTimeMillis();
        opLog.now(product);
        oplog(opLog);
        
        //数据变更通知
        String origId = Optional.ofNullable(orig).map(Product::getId).orElse(null);
        if(!product.id.equals(origId)){
            if(origId!=null) onDataChange(origId, null, product.lastModify);
            onDataChange(product.id, null, product.lastModify);
        }else{
            mapper.setLastModify(product.lastModify, product.id);
        }
        
        return JsonResult.success(product);
    }
    
    @Write
    @Transactional
    @RequestMapping("sort")
    public JsonResult sort(
            @RequestParam ListInt autoIds) {
        return sort(autoIds, mapper, "产品");
    }
    
    @Write
    @Transactional
    @RequestMapping("del")
    public JsonResult del(@RequestParam int autoId) {
        Product orig = mapper.selectById(autoId);
        if(orig==null) return JsonResult.success();
        
        OpLog opLog = OpLog.create(autoId);
        opLog.action = "删除产品";
        opLog.orig(orig);
        
        mapper.deleteById(autoId);
        //联动删除
        paramMapper.onProductDelete(autoId);
        accessMapper.onProductDelete(autoId);
        permissionMapper.onProductDelete(autoId);
        roleMapper.onProductDelete(autoId);
        roleMapper.onProductDelete_access(autoId);
        roleMapper.onProductDelete_permission(autoId);
        
        visitorMapper.onProductDelete_param(autoId);
        visitorMapper.onProductDelete_role(autoId);
        visitorMapper.onProductDelete_access(autoId);
        visitorMapper.onProductDelete_permission(autoId);
        
        userMapper.onProductDelete_param(autoId);
        userMapper.onProductDelete_role(autoId);
        userMapper.onProductDelete_access(autoId);
        userMapper.onProductDelete_param(autoId);
        
        oplog(opLog);
        
        onDataChange(orig.id, null, null);
        
        return JsonResult.success();
    }

}