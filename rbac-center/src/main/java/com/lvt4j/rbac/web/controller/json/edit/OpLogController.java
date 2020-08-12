package com.lvt4j.rbac.web.controller.json.edit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lvt4j.rbac.dao.OpLogMapper;
import com.lvt4j.rbac.dao.ProductMapper;
import com.lvt4j.rbac.db.lock.Read;
import com.lvt4j.rbac.dto.JsonResult;
import com.lvt4j.rbac.po.OpLog;
import com.lvt4j.rbac.po.Product;

/**
 *
 * @author LV on 2020年8月5日
 */
@RestController
@RequestMapping("edit/oplogs")
class OpLogController {

    @Autowired
    private OpLogMapper mapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Read
    @RequestMapping
    public JsonResult oplogs(
            OpLog.Query query) {
        Pair<Long, List<OpLog>> pair = mapper.list(query);
        Map<Integer, Product> pros = LazyMap.lazyMap(new HashMap<>(), productMapper::selectById);
        pair.getRight().stream().map(l->l.proAutoId).filter(Objects::nonNull).distinct().forEach(pros::get);
        return JsonResult.success()
                .dataPut("count", pair.getLeft())
                .dataPut("oplogs", pair.getRight())
                .dataPut("pros", pros);
    }
    
}