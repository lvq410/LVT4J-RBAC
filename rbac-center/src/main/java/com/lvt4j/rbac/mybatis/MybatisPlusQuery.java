package com.lvt4j.rbac.mybatis;

import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvt4j.rbac.dto.Pager;

/**
 * 辅助Query构造MybatisPlus查询用QueryWrapper和Page<br>
 * 一般与{@link AbstractQuery}结合使用<br>
 * 例<pre>
 * class Query implements MybatisPlusQuery&lt;Entity>{
 *  Integer id;
 *  Long age,ageFloor,ageCeiling;
 *  public QueryWrapper&lt;T> toWrapper(){
 *   QueryWrapper&lt;Entity> wrapper = Wrappers.query();
 *    eqWrapper(wrapper, "id", id);
 *    rangeWrapper(wrapper, "age", age, ageFloor, ageCeiling);
 *    sortsWrapper(wrapper);
 *    return wrapper;
 *  }
 * }
 * </pre>
 * @author LV on 2019年4月12日
 */
public interface MybatisPlusQuery<T> {

    /**
     * 转为MybatisPlus查询用QueryWrapper<br>
     * ，在本方法的实现中，可以使用本接口提供的辅助方法{@link #eqWrapper}、{@link #rangeWrapper}等<br>
     * ，注意最后不要漏了{@link #sortsWrapper}加入排序信息
     */
    QueryWrapper<T> toWrapper();
    
    QueryWrapper<T> toWrapperWithoutSort();
    
    
    /** 转为MybatisPlus查询用Page，如果{@link #getPager()}==null，返回null */
    default Page<T> toPage(){
        Pager pager = getPager();
        if(pager==null) return null;
        return new Page<>(pager.getPageNo(), pager.getPageSize());
    }
    
    /** 返回分页信息 */
    Pager getPager();
    
    /** value不为null则在wrapper中加入column!=value条件 */
    default void neWrapper(QueryWrapper<T> wrapper, String column, Integer value) {
        if(value==null) return;
        wrapper.ne(column, value);
    }
    
    /** val不为null则在wrapper中加入column=val条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, Boolean value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    /** value不为null则在wrapper中加入column=value条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, Integer value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    /** value不为null则在wrapper中加入column=value条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, Long value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    /** value不为null则在wrapper中加入column=value条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, Double value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    /** value不为null则在wrapper中加入column=value条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, Date value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    /** value不为null则在wrapper中加入column=value条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, String value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    /** value不为null则在wrapper中加入column=value条件<br>
     * 枚举, IntEnum/StrEnum类取其value值,其他枚举读取其name()值 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, Enum<?> value) {
        if(value==null) return;
        else wrapper.eq(column, value.name());
    }
    /** value不为null则在wrapper中加入column=value条件 */
    default void eqWrapper(QueryWrapper<T> wrapper, String column, BigDecimal value) {
        if(value==null) return;
        wrapper.eq(column, value);
    }
    
    /** value不为null/空字符串则在wrapper中加入column like '%value%'条件 */
    default void likeWrapper(QueryWrapper<T> wrapper, String column, CharSequence value) {
        if(StringUtils.isEmpty(value)) return;
        wrapper.like(column, value);
    }
    /** value不为null/空字符串则在wrapper中加入column like '%value%' or 条件 */
    default void multiLikeWrapper(QueryWrapper<T> wrapper, CharSequence value, String... columns) {
        if(StringUtils.isBlank(value)) return;
        if(ArrayUtils.isEmpty(columns)) return;
        wrapper.and(true, t->{
            for(int i=0; i<columns.length; i++){
                if(i!=0) t = t.or();
                t.like(columns[i], value);
            }
            return t;
        });
    }
    
    
    
    /** value不为null则在wrapper中加入column=value条件<br>
     * floor不为null则在wrapper中加入column>=floor条件<br>
     * ceiling不为null则在wrapper中加入column&lt;=ceiling条件 */
    default void rangeWrapper(QueryWrapper<T> wrapper, String column, Object value, Object floor, Object ceiling) {
        if(value!=null) wrapper.eq(column, value);
        if(floor!=null) wrapper.ge(column, floor);
        if(ceiling!=null) wrapper.le(column, ceiling);
    }
    
    /** exist不为null时，真则在wrapper中加入column is not null条件，假则相反 */
    default void existWrapper(QueryWrapper<T> wrapper, String column, Boolean exist) {
        if(exist==null) return;
        if(exist) wrapper.isNotNull(column);
        else wrapper.isNull(column);
    }
    
    /** values不为null/空,则在wrapper中加入column in values条件 */
    default void inWrapper(QueryWrapper<T> wrapper, String column, int[] values) {
        if(ArrayUtils.isEmpty(values)) return;
        wrapper.in(column, (Object[])ArrayUtils.toObject(values));
    }
    /** values不为null/空,则在wrapper中加入column in values条件 */
    default void inWrapper(QueryWrapper<T> wrapper, String column, long[] values) {
        if(ArrayUtils.isEmpty(values)) return;
        wrapper.in(column, (Object[])ArrayUtils.toObject(values));
    }
    /** values不为null/空,则在wrapper中加入column in values条件 */
    default void inWrapper(QueryWrapper<T> wrapper, String column, String[] values) {
        if(ArrayUtils.isEmpty(values)) return;
        wrapper.in(column, (Object[])values);
    }
    /** values不为null/空,则在wrapper中加入column in values条件<br>
     * 枚举，IntEnum、StrEnum转为其value值，普通枚举使用name()值 */
    default void inWrapper(QueryWrapper<T> wrapper, String column, Enum<?>[] values) {
        if(ArrayUtils.isEmpty(values)) return;
        Object[] vals = new Object[values.length];
        vals = Stream.of(values).map(Enum::name).toArray();
        wrapper.in(column, vals);
    }
    /** vals不为null/空,则在wrapper中加入column in vals条件<br>
     * 数值类、字符串类直接传入<br>
     * 枚举类，IntEnum、StrEnum转为其value值，普通枚举使用name()值 */
    default void inWrapper(QueryWrapper<T> wrapper, String column, Collection<?> values) {
        if(CollectionUtils.isEmpty(values)) return;
        Object first = values.stream().findFirst().get();
        if(first instanceof Enum) values = values.stream().map(Enum.class::cast).map(Enum::name).collect(toSet());
        wrapper.in(column, values);
    }
    
}
