package com.devil.fission.machine.example.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devil.fission.machine.example.service.entity.SysUserEntity;

import java.util.List;

/**
 * 运营用户表 服务接口.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
public interface ISysUserService {
    
    /**
     * 分页查询SysUserEntity列表.
     *
     * @param page   查询页码
     * @param size   查询行数
     * @param entity 查询实体
     * @return 数据分页集合
     */
    Page<SysUserEntity> queryPage(Integer page, Integer size, SysUserEntity entity);
    
    /**
     * 不分页查询SysUserEntity列表.
     *
     * @param entity 查询实体
     * @return 列表
     */
    List<SysUserEntity> queryList(SysUserEntity entity);
    
    /**
     * 根据主键查询SysUser详情.
     *
     * @param userId 主键
     * @return 实体
     */
    SysUserEntity queryById(Long userId);
    
    /**
     * 根据主键集合查询SysUser列表.
     *
     * @param userIds 主键集合
     * @return 列表
     */
    List<SysUserEntity> queryByIds(List<Long> userIds);
    
    /**
     * 新增一条SysUserEntity记录.
     *
     * @param entity 新增实体
     * @return 是否成功
     */
    boolean insert(SysUserEntity entity);
    
    /**
     * 根据主键更新一条SysUserEntity记录（空值不处理）.
     *
     * @param entity 更新实体
     * @return 是否成功
     */
    boolean update(SysUserEntity entity);
    
    /**
     * 删除一条SysUserEntity记录.
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Long id);
    
    /**
     * 批量删除.
     *
     * @param userIds 主键集合
     * @return 是否成功
     */
    boolean deleteByIds(List<Long> userIds);
    
}

