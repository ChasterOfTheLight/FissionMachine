package com.devil.fission.machine.common.response;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应.
 *
 * @author devil
 * @date Created in 2022/12/12 11:48
 */

public class PageData<T> implements Serializable {
    
    private static final long serialVersionUID = -6696629061875374149L;
    
    /**
     * 总记录数.
     */
    @ApiModelProperty(value = "总记录数")
    private long totalCount;
    
    /**
     * 每页记录数.
     */
    @ApiModelProperty(value = "每页记录数")
    private long pageSize;
    
    /**
     * 总页数.
     */
    @ApiModelProperty(value = "总页数")
    private long totalPage;
    
    /**
     * 当前页数.
     */
    @ApiModelProperty(value = "当前页数")
    private long currPage;
    
    /**
     * 列表数据.
     */
    private List<T> list;
    
    public PageData() {
    
    }
    
    /**
     * 空数据.
     */
    public static <T> PageData<T> empty(long pageSize, long currPage) {
        return new PageData<>(Collections.emptyList(), 0, pageSize, currPage);
    }
    
    /**
     * 分页.
     *
     * @param list       列表数据
     * @param totalCount 总记录数
     * @param pageSize   每页记录数
     * @param currPage   当前页数
     */
    public PageData(List<T> list, long totalCount, long pageSize, long currPage) {
        this.list = list;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.currPage = currPage;
        this.totalPage = pageSize > 0 ? (totalCount + pageSize - 1) / pageSize : 0;
    }
    
    /**
     * the totalCount.
     */
    public long getTotalCount() {
        return totalCount;
    }
    
    /**
     * totalCount : the totalCount to set.
     */
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
    
    /**
     * the pageSize.
     */
    public long getPageSize() {
        return pageSize;
    }
    
    /**
     * pageSize : the pageSize to set.
     */
    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
    
    /**
     * the totalPage.
     */
    public long getTotalPage() {
        return totalPage;
    }
    
    /**
     * totalPage : the totalPage to set.
     */
    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }
    
    /**
     * the currPage.
     */
    public long getCurrPage() {
        return currPage;
    }
    
    /**
     * currPage : the currPage to set.
     */
    public void setCurrPage(long currPage) {
        this.currPage = currPage;
    }
    
    /**
     * the list.
     */
    public List<T> getList() {
        return list;
    }
    
    /**
     * list : the list to set.
     */
    public void setList(List<T> list) {
        this.list = list;
    }
}
