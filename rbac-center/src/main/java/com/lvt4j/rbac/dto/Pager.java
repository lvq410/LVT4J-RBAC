package com.lvt4j.rbac.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 分页信息数据结构
 * @author LV on 2019年4月11日
 */
@Data
public class Pager implements Serializable {
    private static final long serialVersionUID = 8440831246021118908L;
    
    public static final int Default_PageNo = 1;
    public static final int Default_PageSize = 10;
    
    /** 页码 */
    private int pageNo;
    /** 页大小 */
    private int pageSize;
    
    /** 默认第一页，每页10条 */
    public Pager() {
        this(Default_PageNo, Default_PageSize);
    }
    
    /**
     * 初始化一个自定义页信息
     * @param pageNo 小于1会重置为{@link #Default_PageNo}
     * @param pageSize 小于1会重置为{@link #Default_PageSize}
     */
    public Pager(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        init();
    }
    /**
     * 设置当前页码
     * @param pageNo 小于1会重置为{@link #Default_PageNo}
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
        init();
    }
    /**
     * 设置当前页大小 
     * @param pageSize 小于1会重置为{@link #Default_PageSize}
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        init();
    }

    /**
     * 翻下一页(pageNo加1)
     * @return 加1后的pageNo
     */
    public int nextPage() {
        pageNo++;
        init();
        return pageNo;
    }
    
    /**  */
    /**
     * 翻上一页(pageNo减1)
     * @return 减1后的pageNo;
     */
    public int prevPage() {
        pageNo--;
        init();
        return pageNo;
    }
    
    private void init(){
        if(pageNo<1) pageNo = Default_PageNo;
        if(pageSize<1) pageSize = Default_PageSize;
    }
    
    /** 当前页码 */
    public int getPageNo() {
        return pageNo;
    }
    
    /** 当前页大小 */
    public int getPageSize() {
        return pageSize;
    }
    
    /** 起始偏移量，如用于mysql：limit offset,length */
    @JsonIgnore
    public int getOffset() {
        return (pageNo-1)*pageSize;
    }
    
    /** 读取条数，与pageSize相同，如用于mysql：limit offset,length */
    @JsonIgnore
    public int getLength() {
        return pageSize;
    }
    
    /** 最后一条偏移量(=offset+length)，如用于深度分页判定 */
    @JsonIgnore
    public int getEnd() {
        return getOffset()+pageSize;
    }
}