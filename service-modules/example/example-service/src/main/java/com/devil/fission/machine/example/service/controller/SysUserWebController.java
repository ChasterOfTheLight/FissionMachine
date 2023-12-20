package com.devil.fission.machine.example.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devil.fission.machine.common.exception.ServiceException;
import com.devil.fission.machine.common.response.PageData;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.api.dto.SysUserDto;
import com.devil.fission.machine.example.api.vo.SysUserQueryVo;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.feign.SysUserFeignClient;
import com.devil.fission.machine.example.service.param.SysUserDeleteParam;
import com.devil.fission.machine.example.service.param.SysUserInsertParam;
import com.devil.fission.machine.example.service.param.SysUserPageQueryParam;
import com.devil.fission.machine.example.service.param.SysUserUpdateParam;
import com.devil.fission.machine.example.service.service.ExampleService;
import com.devil.fission.machine.example.service.service.ISysUserService;
import com.devil.fission.machine.example.service.service.NacosFlagService;
import com.devil.fission.machine.example.service.service.impl.SysUserServiceImplManager;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 运营用户表 对外接口服务控制器.
 *
 * @author devil
 * @date 2022-12-12 10:46:52
 */
@Slf4j
@Api(value = "运营用户表相关接口", tags = {"WEB端-运营用户表相关接口"})
@RequestMapping("/sysUser/web")
@RestController
public class SysUserWebController {
    
    private final ISysUserService sysUserService;
    
    private final SysUserServiceImplManager sysUserServiceImplManager;
    
    private final SysUserFeignClient sysUserFeignClient;
    
    private final NacosFlagService nacosFlagService;
    
    private final ExampleService exampleService;
    
    private final RedissonClient redissonClient;
    
    public SysUserWebController(ISysUserService sysUserService, SysUserServiceImplManager sysUserServiceImplManager,
            SysUserFeignClient sysUserFeignClient, NacosFlagService nacosFlagService, ExampleService exampleService, RedissonClient redissonClient) {
        this.sysUserService = sysUserService;
        this.sysUserServiceImplManager = sysUserServiceImplManager;
        this.sysUserFeignClient = sysUserFeignClient;
        this.nacosFlagService = nacosFlagService;
        this.exampleService = exampleService;
        this.redissonClient = redissonClient;
        log.info("SysUserWebController init");
    }
    
    /**
     * 分页查询列表.
     *
     * @param param 分页查询参数
     * @return 数据分页集合
     */
    @PostMapping(value = "/pageList", produces = {"application/json"})
    @ApiOperation(value = "分页查询运营用户表列表", notes = "分页查询运营用户表列表")
    public Response<PageData<SysUserQueryVo>> pageList(@Valid @RequestBody SysUserPageQueryParam param) {
        Page<SysUserEntity> queryPage = sysUserService.queryPage(param.getPageNum(), param.getPageSize(), new SysUserEntity());
        if (CollectionUtils.isEmpty(queryPage.getRecords())) {
            return Response.success(PageData.empty(param.getPageNum(), param.getPageSize()));
        }
        
        // test feign
        SysUserQueryVo sysUserQueryVo = sysUserFeignClient.info(SysUserDto.builder().userId(5L).build()).getData();
        log.info(new Gson().toJson(sysUserQueryVo));
        
        return Response.success(
                new PageData<>(queryPage.getRecords().stream().map(SysUserEntity::convert2Vo).collect(Collectors.toList()), queryPage.getTotal(),
                        queryPage.getSize(), queryPage.getCurrent()));
    }
    
    /**
     * 查询列表.
     */
    @PostMapping(value = "/list", produces = {"application/json"})
    @ApiOperation(value = "查询运营用户表列表", notes = "查询运营用户表列表")
    public Response<List<SysUserQueryVo>> list() {
        List<SysUserEntity> sysUserEntities = sysUserService.queryList(new SysUserEntity());
        if (CollectionUtils.isEmpty(sysUserEntities)) {
            return Response.success(Collections.emptyList());
        }
        
        return Response.success(sysUserEntities.stream().map(SysUserEntity::convert2Vo).collect(Collectors.toList()));
    }
    
    /**
     * 详情.
     *
     * @param dto 详情查询参数
     * @return 详情
     */
    @PostMapping(value = "/info", produces = {"application/json"})
    @ApiOperation(value = "运营用户表详情查询", notes = "运营用户表详情查询")
    public Response<SysUserQueryVo> info(@Valid @RequestBody SysUserDto dto) {
        String lockKey = "sysUser:" + "lock:" + dto.getUserId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(1L, 3000L, TimeUnit.MILLISECONDS);
            if (locked) {
                SysUserEntity sysUser = sysUserService.queryById(dto.getUserId());
                SysUserQueryVo vo = SysUserEntity.convert2Vo(sysUser);
                return Response.success(vo);
            } else {
                return Response.success(null);
            }
        } catch (InterruptedException e) {
            throw new ServiceException("运营用户表详情查询加锁失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    /**
     * 批量详情.
     *
     * @param dtoList 批量详情查询参数
     * @return 详情
     */
    @PostMapping(value = "/infos", produces = {"application/json"})
    @ApiOperation(value = "运营用户表批量详情查询", notes = "运营用户表批量详情查询")
    public Response<List<SysUserQueryVo>> infos(@Valid @RequestBody List<SysUserDto> dtoList) {
        List<SysUserEntity> sysUserEntities = sysUserServiceImplManager.queryByIds(
                dtoList.stream().map(SysUserDto::getUserId).collect(Collectors.toList()));
        List<SysUserQueryVo> voList = sysUserEntities.stream().filter(Objects::nonNull).map(SysUserEntity::convert2Vo).collect(Collectors.toList());
        return Response.success(voList);
    }
    
    /**
     * 新增.
     *
     * @param param 新增参数
     * @return 成功 | 失败
     */
    @PostMapping(value = "/save", produces = {"application/json"})
    @ApiOperation(value = "新增运营用户表数据", notes = "新增运营用户表数据")
    public Response<Boolean> save(@Valid @RequestBody SysUserInsertParam param) {
        boolean result = sysUserService.insert(SysUserEntity.convertFromInsertParam(param));
        return result ? Response.success(true) : Response.error("新增失败", false);
    }
    
    /**
     * 更新.
     *
     * @param param 更新参数
     * @return 成功 | 失败
     */
    @PostMapping(value = "/update", produces = {"application/json"})
    @ApiOperation(value = "更新运营用户表数据", notes = "更新运营用户表数据")
    public Response<Boolean> update(@Valid @RequestBody SysUserUpdateParam param) {
        boolean result = sysUserService.update(SysUserEntity.convertFromUpdateParam(param));
        return result ? Response.success(true) : Response.error("更新失败", false);
    }
    
    /**
     * 删除.
     *
     * @param param 删除参数
     * @return 成功 | 失败
     */
    @PostMapping(value = "/delete", produces = {"application/json"})
    @ApiOperation(value = "删除运营用户表数据", notes = "删除运营用户表数据")
    public Response<Boolean> delete(@Valid @RequestBody SysUserDeleteParam param) {
        boolean result = sysUserService.deleteById(param.getUserId());
        return result ? Response.success(true) : Response.error("删除失败", false);
    }
    
    /**
     * nacos配置刷新实验.
     */
    @ApiIgnore
    @RequestMapping(value = "/flag", produces = {"application/json"})
    public Response<String> flag() {
        return Response.success(nacosFlagService.flag());
    }
    
    /**
     * queryExample.
     */
    @ApiIgnore
    @RequestMapping(value = "/queryExample", produces = {"application/json"})
    public Response<String> queryExample() {
        return Response.success(exampleService.queryExample("123"));
    }
    
}
