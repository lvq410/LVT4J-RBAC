package com.lvt4j.rbac.po;

import static com.lvt4j.rbac.Utils.parseIPFromReq;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt4j.rbac.dto.ListInt;
import com.lvt4j.rbac.dto.ListStr;
import com.lvt4j.rbac.dto.Pager;
import com.lvt4j.rbac.mybatis.MybatisPlusQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * 操作日志
 * @author LV on 2020年5月26日
 */
@Data
@TableName("oplog")
public class OpLog {

    private static ObjectMapper ObjectMapper = new ObjectMapper();
    
    static{
        ObjectMapper.setSerializationInclusion(Include.NON_NULL);
    }
    
    public String operator;
    public String ip;
    public String action;
    public Date time;
    public Integer proAutoId;
    public String orig;
    public String now;
    public String comment;
    
    public static OpLog create(Integer proAutoId) {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        OpLog opLog = new OpLog();
        opLog.operator = (String) req.getAttribute("operator");
        opLog.ip = parseIPFromReq(req);
        opLog.time = new Date();
        opLog.proAutoId = proAutoId;
        return opLog;
    }
    
    @SneakyThrows
    public void orig(Object obj) {
        if(obj==null) return;
        this.orig = ObjectMapper.writeValueAsString(obj);
    }
    
    @SneakyThrows
    public void now(Object obj) {
        if(obj==null) return;
        this.now = ObjectMapper.writeValueAsString(obj);
    }
    
    @Data@Builder
    @NoArgsConstructor@AllArgsConstructor
    public static class Query implements MybatisPlusQuery<OpLog> {
        public ListStr operators;
        public ListStr ips;
        public ListStr actions;
        public Date timeFloor, timeCeiling;
        public ListInt proAutoIds;
        public String keyword;
        public boolean ascOrDesc;
        public Pager pager;
        
        @Override
        public QueryWrapper<OpLog> toWrapperWithoutSort() {
            QueryWrapper<OpLog> wrapper = Wrappers.query();
            inWrapper(wrapper, "operator", operators);
            inWrapper(wrapper, "ip", ips);
            inWrapper(wrapper, "action", actions);
            rangeWrapper(wrapper, "time", null, timeFloor, timeCeiling);
            inWrapper(wrapper, "proAutoId", proAutoIds);
            multiLikeWrapper(wrapper, keyword, "orig","now");
            return wrapper;
        }
        
        @Override
        public QueryWrapper<OpLog> toWrapper() {
            QueryWrapper<OpLog> wrapper = toWrapperWithoutSort();
            wrapper.orderBy(true, ascOrDesc, "time");
            return wrapper;
        }
    }
    
}