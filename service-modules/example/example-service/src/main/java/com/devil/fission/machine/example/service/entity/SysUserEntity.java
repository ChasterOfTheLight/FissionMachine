package com.devil.fission.machine.example.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devil.fission.machine.example.api.vo.SysUserQueryVo;
import com.devil.fission.machine.example.service.param.SysUserInsertParam;
import com.devil.fission.machine.example.service.param.SysUserUpdateParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 运营用户表 实体.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@TableName("sys_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID.
     */
    @TableId(type = IdType.AUTO)
    private Long userId;
    
    /**
     * 用户账号.
     */
    private String userName;
    
    /**
     * 密码.
     */
    private String userPassword;
    
    /**
     * 是否启用 1是0 否；默认1.
     */
    private Integer isEnabled;
    
    /**
     * 最后登录IP.
     */
    private String lastLoginIp;
    
    /**
     * 最后登录时间.
     */
    private Date lastLoginDate;
    
    /**
     * 创建人ID.
     */
    private Long createdBy;
    
    /**
     * 创建时间.
     */
    private Date createdTime;
    
    /**
     * 修改人ID.
     */
    private Long updatedBy;
    
    /**
     * 修改时间.
     */
    private Date updatedTime;
    
    /**
     * 排序参数（只参与查询  key：排序字段  value：是否是asc）.
     */
    @TableField(exist = false)
    private LinkedHashMap<String, Boolean> orderBy;
    
    /**
     * entity转换成vo.
     */
    public static SysUserQueryVo convert2Vo(SysUserEntity entity) {
        SysUserQueryVo vo = SysUserQueryVo.builder().build();
        if (entity != null) {
            BeanUtils.copyProperties(entity, vo);
        }
        return vo;
    }
    
    /**
     * 从insertParam转换成entity.
     */
    public static SysUserEntity convertFromInsertParam(SysUserInsertParam insertParam) {
        SysUserEntity entity = SysUserEntity.builder().build();
        if (insertParam != null) {
            BeanUtils.copyProperties(insertParam, entity);
        }
        return entity;
    }
    
    /**
     * 从updateParam转换成entity.
     */
    public static SysUserEntity convertFromUpdateParam(SysUserUpdateParam updateParam) {
        SysUserEntity entity = SysUserEntity.builder().build();
        if (updateParam != null) {
            BeanUtils.copyProperties(updateParam, entity);
        }
        return entity;
    }
    
}
