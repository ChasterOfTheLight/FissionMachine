package com.devil.fission.machine.example.service.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devil.fission.machine.common.Constants;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.event.ExampleEvent;
import com.devil.fission.machine.example.service.mapper.SysUserMapper;
import com.devil.fission.machine.example.service.service.ISysUserService;
import com.lmax.disruptor.RingBuffer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
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
    
    public static final String CACHE_PREFIX = "SysUser:id:";
    
    public static final int CACHE_EXPIRE = 3600;
    
    @CreateCache(name = CACHE_PREFIX, expire = CACHE_EXPIRE)
    private Cache<String, SysUserEntity> cache;
    
    @Resource(name = "exampleEventRingBuffer")
    private RingBuffer<ExampleEvent> ringBuffer;
    
    @Override
    public Page<SysUserEntity> queryPage(Integer page, Integer size, SysUserEntity entity) {
        if (page == 0) {
            page = 1;
        }
        // 默认10条
        if (size == 0) {
            size = 10;
        }
        // 最大100条
        if (size > Constants.COMMON_MAX_PAGE_NUM) {
            size = 100;
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
    
    @DS("master")
    @Override
    public List<SysUserEntity> queryList(SysUserEntity entity) {
        return super.list((assemblyWrapper(entity)));
    }
    
    @Cached(name = CACHE_PREFIX, key = "#userId", expire = CACHE_EXPIRE)
    @Override
    public SysUserEntity queryById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 发送队列消息，异步处理
        // 获取下一个Event槽的下标
        long sequence = ringBuffer.next();
        try {
            // 给Event填充数据
            ExampleEvent event = ringBuffer.get(sequence);
            event.setValue("666");
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            // 发布Event，激活观察者去消费，将sequence传递给该消费者
            // 注意最后的publish方法必须放在finally中以确保必须得到调用；如果某个请求的sequence未被提交将会堵塞后续的发布操作或者其他的producer
            ringBuffer.publish(sequence);
        }
        
        return super.getById(userId);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insert(SysUserEntity entity) {
        if (entity == null) {
            return false;
        }
        entity.setCreatedTime(new Date());
        return super.save(entity);
    }
    
    @CacheInvalidate(name = CACHE_PREFIX, key = "#entity.userId", condition = "#result==true")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysUserEntity entity) {
        if (entity == null || entity.getUserId() == null) {
            return false;
        }
        entity.setUpdatedTime(new Date());
        return super.updateById(entity);
    }
    
    @CacheInvalidate(name = CACHE_PREFIX, key = "#userId", condition = "#result==true")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(Long userId) {
        SysUserEntity entity = new SysUserEntity();
        entity.setUserId(userId);
        entity.setIsEnabled(0);
        entity.setUpdatedTime(new Date());
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
        entityWrapper.lambda().select(SysUserEntity::getUserId, SysUserEntity::getUserName, SysUserEntity::getIsEnabled);
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
