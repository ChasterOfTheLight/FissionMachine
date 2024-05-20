package com.devil.fission.machine.example.service.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.mapper.SysUserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserServiceImpl } unit test.
 *
 * @author Devil
 * @date Created in 2024/5/17 14:46
 */
@RunWith(MockitoJUnitRunner.class)
public class SysUserServiceImplTest {
    
    @InjectMocks
    private SysUserServiceImpl sysUserService;
    
    @Mock
    private SysUserMapper sysUserMapper;
    
    @Test
    public void testQueryList() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysUserEntity.class);
        when(sysUserMapper.selectList(any())).thenReturn(new ArrayList<>());
        Assert.assertTrue(sysUserService.queryList(SysUserEntity.builder().build()).isEmpty());
    }
}