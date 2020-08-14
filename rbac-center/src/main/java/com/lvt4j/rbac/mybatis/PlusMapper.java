package com.lvt4j.rbac.mybatis;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 基于MybatisPlus的BaseMapper，加入一些新通用方法
 * @author LV on 2019年4月12日
 * @param <T>
 */
public interface PlusMapper<T> extends BaseMapper<T> {

    /**
     * 根据查询条件返回查询结果<br>
     * 查询条件可指定query.pager是否为null，null返回所有匹配结果否则返回指定分页数据
     * @param query
     * @return Pair<总数, 当前页数据>
     */
    default Pair<Long, List<T>> list(MybatisPlusQuery<T> query) {
        List<T> ts; long count;
        QueryWrapper<T> wrapper = query.toWrapper();
        if(query.getPager()==null) {
            ts = selectList(wrapper);
            count = ts.size();
        }else {
            IPage<T> page = selectPage(query.toPage(), wrapper);
            ts = page.getRecords();
            count = page.getTotal();
        }
        return Pair.of(count, ts);
    }
    
}
