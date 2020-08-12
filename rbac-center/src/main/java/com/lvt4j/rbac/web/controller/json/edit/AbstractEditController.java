package com.lvt4j.rbac.web.controller.json.edit;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvt4j.rbac.dao.AccessMapper;
import com.lvt4j.rbac.dao.OpLogMapper;
import com.lvt4j.rbac.dao.PermissionMapper;
import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.dao.RoleMapper;
import com.lvt4j.rbac.dao.SequenceSetter;
import com.lvt4j.rbac.dao.VisitorMapper;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.mybatis.MybatisPlusQuery;
import com.lvt4j.rbac.mybatis.PlusMapper;
import com.lvt4j.rbac.po.Access;
import com.lvt4j.rbac.po.Entity;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Permission;
import com.lvt4j.rbac.po.Product;
import com.lvt4j.rbac.po.Role;
import com.lvt4j.rbac.service.AuthChangeNotifier;
import com.lvt4j.rbac.web.controller.json.Checker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author LV on 2020年8月5日
 */
@Slf4j
abstract class AbstractEditController {

    @Autowired
    private OpLogMapper opLogMapper;
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private AccessMapper accessMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private VisitorMapper visitorMapper;
    
    @Autowired
    private AuthChangeNotifier notifier;
    
    /** 通用查询接口 */
    protected <T> JsonResult list(PlusMapper<T> mapper, MybatisPlusQuery<T> query) {
        Pair<Long, List<T>> pair = mapper.list(query);
        return JsonResult.success()
                .dataPut("count", pair.getLeft())
                .dataPut("models", pair.getRight());
    }
    
    /** 通用对象获取接口 */
    protected <T> JsonResult get(PlusMapper<T> mapper, MybatisPlusQuery<T> query) {
        return JsonResult.success(mapper.selectOne(query.toWrapper()));
    }
    
    /** 通用排序接口 */
    protected <E extends Entity> JsonResult sort(ListInt autoIds, PlusMapper<E> mapper, String modelDes) {
        if(autoIds.isEmpty()) return JsonResult.success();
        List<Integer> seqs = new ArrayList<>(autoIds.size());
        List<E> pos = new ArrayList<>(autoIds.size());
        for(Integer autoId : autoIds){
            E po = mapper.selectById(autoId);
            Checker.isTrue(po!=null, "%s[autoId=%s]不存在", modelDes, autoId);
            pos.add(po);
            seqs.add(po.getSeq());
        }
        OpLog opLog = OpLog.create(null);
        opLog.action = "排序"+modelDes;
        List<E> orig = new ArrayList<>(pos);
        orig.sort((p0,p1)->Integer.compare(p0.getSeq(), p1.getSeq()));
        opLog.orig(orig);
        
        Collections.sort(seqs);
        SequenceSetter sequenceSetter = (SequenceSetter) mapper;
        for(int i=0; i<seqs.size(); i++){
            sequenceSetter.setSeq(seqs.get(i), autoIds.get(i));
        }
       
        opLog.now(pos);
        oplog(opLog);
        
        return JsonResult.success();
    }
    
    /** 记录操作日志 */
    protected void oplog(OpLog opLog) {
        try{
            opLogMapper.insert(opLog);
        }catch(Exception e){
            log.warn("记录操作日志失败:{}", opLog, e);
        }
    }
    
    /** 一次计算权限及来源的任务 */
    @RequiredArgsConstructor
    protected class AuthCal {
        private final boolean isUser;
        private final Integer proAutoId;
        private final List<Integer> roleAutoIds;
        private final List<Integer> accessAutoIds;
        private final List<Integer> permissionAutoIds;
        
        private Set<Integer> allRoleAutoIds = new TreeSet<>();
        private Set<Integer> allAccessAutoIds = new TreeSet<>();
        private Set<Integer> allPermissionAutoIds = new TreeSet<>();
        
        @Getter
        private List<AuthDesc<Role>> allRoles = new LinkedList<>();
        @Getter
        private List<AuthDesc<Access>> allAccesses = new LinkedList<>();
        @Getter
        private List<AuthDesc<Permission>> allPermissions = new LinkedList<>();
        
        public JsonResult run() {
            if(isUser){
                addRoles(visitorMapper.roleAutoIds(proAutoId), "游客");
                addAccesses(visitorMapper.accessAutoIds(proAutoId), "游客");
                addPermissions(visitorMapper.permissionAutoIds(proAutoId), "游客");
            }
            addRoles(roleAutoIds, "");
            addAccesses(accessAutoIds, "");
            addPermissions(permissionAutoIds, "");
            return JsonResult.success(this);
        }
        private void addRoles(List<Integer> roleAutoIds, String from) {
            List<Role> roles = addAuths(roleAutoIds, from, roleMapper, allRoleAutoIds, allRoles);
            if(roles==null) return;
            if(StringUtils.isNotBlank(from)) from += " > ";
            for(Role role : roles){
                String des = from+"角色["+role.name+"]";
                addAccesses(roleMapper.accessAutoIds(role.autoId), des);
                addPermissions(roleMapper.permissionAutoIds(role.autoId), des);
            }
        }
        private void addAccesses(List<Integer> accessAutoIds, String des) {
            addAuths(accessAutoIds, des, accessMapper, allAccessAutoIds, allAccesses);
        }
        private void addPermissions(List<Integer> permissionAutoIds, String des) {
            addAuths(permissionAutoIds, des, permissionMapper, allPermissionAutoIds, allPermissions);
        }
        
        private <E extends Entity> List<E> addAuths(List<Integer> autoIds, String des, PlusMapper<E> mapper, Set<Integer> allAutoIds, List<AuthDesc<E>> all) {
            List<Integer> deltaAutoIds = autoIds.stream().filter(id->!allAutoIds.contains(id)).collect(toList());
            if(deltaAutoIds.isEmpty()) return null;
            List<E> deltas = deltaAutoIds.stream().map(mapper::selectById).filter(Objects::nonNull).collect(toList());
            if(deltas.isEmpty()) return null;
            deltas.stream().map(Entity::getAutoId).forEach(allAutoIds::add);
            AuthDesc<E> rolesDesc = new AuthDesc<>();
            rolesDesc.des = des;
            rolesDesc.auths = deltas;
            all.add(rolesDesc);
            return deltas;
        }
        
        
    }
    /** 记录一部分权限对象来自于{@link #des} */
    @Getter
    class AuthDesc<A>{
        /** 权限来源，如"角色XXX"、"游客"等或""表示单独分配 */
        public String des;
        /** 权限清单 */
        public List<A> auths = new LinkedList<>();
    }
    
    /** 数据变更后执行通知 */
    protected void onDataChange(Integer proAutoId){
        Product pro = productMapper.selectById(proAutoId);
        if(pro==null) return;
        onDataChange(pro.id, null, null);
    }
    /** 数据变更后执行通知 */
    protected void onDataChange(String proId, String userId, Long now){
        if(now==null) now = System.currentTimeMillis();
        if(proId==null){
            productMapper.setAllLastModify(now);
        }else{
            productMapper.setLastModify(now, proId);
        }
        notifier.notify(proId, userId);
    }
    
}