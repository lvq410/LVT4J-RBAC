package com.lvt4j.rbac.data.model;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt4j.basic.TDB.Table;

import lombok.Setter;
import lombok.SneakyThrows;

/**
 * 操作日志
 * @author LV on 2020年5月26日
 */
@Table("oplog")
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
    
    @SneakyThrows
    public void setOrig(Object obj) {
        if(obj==null) return;
        this.orig = ObjectMapper.writeValueAsString(obj);
    }
    
    @SneakyThrows
    public void setNow(Object obj) {
        if(obj==null) return;
        this.now = ObjectMapper.writeValueAsString(obj);
    }
    
    @Setter
    public static class Query {
        public String[] operators;
        public String[] ips;
        public String[] actions;
        public Date timeFloor, timeCeiling;
        public int[] proAutoIds;
        public String keyword;
        
        public Pair<String, List<Object>> buildWhereClause() {
            StringBuilder whereClauseBuilder = new StringBuilder();
            List<Object> args = new LinkedList<>();
            if(ArrayUtils.isNotEmpty(operators)){
                whereClauseBuilder.append(" and operator in ( ")
                    .append(Stream.of(operators).map(o->"?").collect(Collectors.joining(",")))
                    .append(") ");
                args.addAll(Arrays.asList(operators));
            }
            if(ArrayUtils.isNotEmpty(ips)){
                whereClauseBuilder.append(" and ip in ( ")
                    .append(Stream.of(ips).map(o->"?").collect(Collectors.joining(",")))
                    .append(") ");
                args.addAll(Arrays.asList(ips));
            }
            if(ArrayUtils.isNotEmpty(actions)){
                whereClauseBuilder.append(" and action in ( ")
                    .append(Stream.of(actions).map(o->"?").collect(Collectors.joining(",")))
                    .append(") ");
                args.addAll(Arrays.asList(actions));
            }
            if(timeFloor!=null){
                whereClauseBuilder.append(" and time >= ? ");
                args.add(timeFloor);
            }
            if(timeCeiling!=null){
                whereClauseBuilder.append(" and time <= ? ");
                args.add(timeCeiling);
            }
            if(ArrayUtils.isNotEmpty(proAutoIds)){
                whereClauseBuilder.append(" and proAutoId in ( ")
                    .append(IntStream.of(proAutoIds).mapToObj(o->"?").collect(Collectors.joining(",")))
                    .append(") ");
                args.addAll(IntStream.of(proAutoIds).boxed().collect(toList()));
            }
            if(StringUtils.isNotBlank(keyword)){
                whereClauseBuilder.append(" and ( orig like ? or now like ?) ");
                args.add("%"+keyword+"%");args.add("%"+keyword+"%");
            }
            if(whereClauseBuilder.length()>0) whereClauseBuilder.replace(0, 5, "").insert(0, " where ");
            return Pair.of(whereClauseBuilder.toString(), args);
        }
    }
    
}