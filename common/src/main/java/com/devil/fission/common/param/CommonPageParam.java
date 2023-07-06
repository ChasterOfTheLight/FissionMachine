package com.devil.fission.common.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 公共分页参数.
 *
 * @author devil
 * @date Created in 2022/12/12 11:39
 */
@ApiModel("分页公共参数")
public class CommonPageParam {
    
    @ApiModelProperty("默认值10  每页行数")
    protected int pageSize = 10;
    
    @ApiModelProperty("默认值1  当前页数，从1开始")
    protected int pageNum = 1;
    
    public CommonPageParam() {
    }
    
    public int getPageSize() {
        return this.pageSize <= 0 ? 10 : (this.pageSize >= 100 ? 100 : this.pageSize);
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getPageNum() {
        return this.pageNum <= 0 ? 1 : this.pageNum;
    }
    
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}
