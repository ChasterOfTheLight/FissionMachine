package com.devil.fission.machine.example.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devil.fission.machine.common.response.PageData;
import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.api.vo.SysUserQueryVo;
import com.devil.fission.machine.example.service.entity.SysUserEntity;
import com.devil.fission.machine.example.service.param.SysUserDeleteParam;
import com.devil.fission.machine.example.service.param.SysUserInfoQueryParam;
import com.devil.fission.machine.example.service.param.SysUserInsertParam;
import com.devil.fission.machine.example.service.param.SysUserPageQueryParam;
import com.devil.fission.machine.example.service.param.SysUserUpdateParam;
import com.devil.fission.machine.example.service.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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
    
    public SysUserWebController(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
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
        
        return Response.success(
                new PageData<>(queryPage.getRecords().stream().map(SysUserEntity::convert2Vo).collect(Collectors.toList()), queryPage.getTotal(),
                        queryPage.getSize(), queryPage.getCurrent()));
    }
    
    /**
     * 详情.
     *
     * @param param 详情查询参数
     * @return 详情
     */
    @PostMapping(value = "/info", produces = {"application/json"})
    @ApiOperation(value = "运营用户表详情查询", notes = "运营用户表详情查询")
    public Response<SysUserQueryVo> info(@Valid @RequestBody SysUserInfoQueryParam param) {
        SysUserEntity sysUser = sysUserService.queryById(param.getUserId());
        SysUserQueryVo vo = SysUserEntity.convert2Vo(sysUser);
        return Response.success(vo);
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
    
}
