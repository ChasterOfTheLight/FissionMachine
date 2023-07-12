package com.devil.fission.machine.example.service.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.mapper.SysUserMapper;
import com.devil.fission.machine.example.service.service.ISysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 运营用户表 服务接口实现.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements ISysUserService {
    
    private static final String CACHE_PREFIX = "SysUser:id:";
    
    @CreateCache(name = CACHE_PREFIX, expire = 3600)
    private Cache<String, SysUserEntity> cache;
    
    @Override
    public Page<SysUserEntity> queryPage(Integer page, Integer size, SysUserEntity entity) {
        if (page == 0) {
            page = 1;
        }
        // 默认10条
        if (size == 0 || size > 10) {
            size = 10;
        }
        Page<SysUserEntity> mybatisPage = new Page<>(page, size);
        // 查询实体处理
        QueryWrapper<SysUserEntity> queryWrapper = assemblyWrapper(entity);
        // 排序处理
        if (entity.getOrderBy() != null && !entity.getOrderBy().isEmpty()) {
            entity.getOrderBy().forEach((key, value) -> mybatisPage.addOrder(new OrderItem(key, value)));
        }
        return page(mybatisPage, queryWrapper);
    }
    
    @Override
    public List<SysUserEntity> queryList(SysUserEntity entity) {
        return super.list((assemblyWrapper(entity)));
    }
    
    @Cached(name = CACHE_PREFIX, key = "#userId", expire = 3600)
    @Override
    public SysUserEntity queryById(Long userId) {
        if (userId == null) {
            return null;
        }
        return super.getById(userId);
    }
    
    @Override
    public List<SysUserEntity> queryByIds(List<Long> ids) {
        List<SysUserEntity> list = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                // 从缓存中拿
                SysUserEntity cachedEntity = cache.get(String.valueOf(id));
                if (cachedEntity != null) {
                    list.add(cachedEntity);
                } else {
                    list.add(queryById(id));
                }
            }
        }
        return list;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insert(SysUserEntity entity) {
        if (entity == null) {
            return false;
        }
        return super.save(entity);
    }
    
    @CacheInvalidate(name = CACHE_PREFIX, key = "#entity.userId", condition = "#result=true")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysUserEntity entity) {
        if (entity == null || entity.getUserId() == null) {
            return false;
        }
        return super.updateById(entity);
    }
    
    @CacheInvalidate(name = CACHE_PREFIX, key = "#userId", condition = "#result=true")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(Long userId) {
        SysUserEntity entity = new SysUserEntity();
        entity.setUserId(userId);
        entity.setIsEnabled(0);
        return super.updateById(entity);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByIds(List<Long> ids) {
        boolean result = false;
        if (ids != null && !ids.isEmpty()) {
            result = true;
            for (Long id : ids) {
                result = result && deleteById(id);
                if (result) {
                    // 删除缓存
                    cache.REMOVE(String.valueOf(id));
                }
            }
        }
        return result;
    }
    
    /**
     * 封装查询实体.
     */
    private QueryWrapper<SysUserEntity> assemblyWrapper(SysUserEntity entity) {
        QueryWrapper<SysUserEntity> entityWrapper = new QueryWrapper<>();
        if (null != entity) {
            entityWrapper.eq(Objects.nonNull(entity.getUserId()), "user_id", entity.getUserId());
            entityWrapper.eq(StringUtils.isNotBlank(entity.getUserName()), "user_name", entity.getUserName());
            entityWrapper.eq(StringUtils.isNotBlank(entity.getUserPassword()), "user_password", entity.getUserPassword());
            entityWrapper.eq(Objects.nonNull(entity.getIsEnabled()), "is_enabled", entity.getIsEnabled());
            entityWrapper.eq(StringUtils.isNotBlank(entity.getLastLoginIp()), "last_login_ip", entity.getLastLoginIp());
            entityWrapper.eq(Objects.nonNull(entity.getCreatedBy()), "created_by", entity.getCreatedBy());
            entityWrapper.eq(Objects.nonNull(entity.getUpdatedBy()), "updated_by", entity.getUpdatedBy());
        }
        return entityWrapper;
    }
    
}
