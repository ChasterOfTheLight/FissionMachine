package com.devil.fission.machine.example.service.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SysUserServiceImplManager.
 *
 * @author Devil
 * @date Created in 2023/8/3 16:46
 */
@Service
public class SysUserServiceImplManager {
    
    private final ISysUserService sysUserService;
    
    public SysUserServiceImplManager(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }
    
    @CreateCache(name = SysUserServiceImpl.CACHE_PREFIX, expire = SysUserServiceImpl.CACHE_EXPIRE)
    private Cache<String, SysUserEntity> cache;
    
    public List<SysUserEntity> queryByIds(List<Long> ids) {
        List<SysUserEntity> list = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                // 从缓存中拿
                SysUserEntity cachedEntity = cache.get(String.valueOf(id));
                if (cachedEntity != null) {
                    list.add(cachedEntity);
                } else {
                    list.add(sysUserService.queryById(id));
                }
            }
        }
        return list;
    }
    
}
