package com.fission.machine.generator.controller;

import com.fission.machine.generator.service.SysGeneratorService;
import com.fission.machine.generator.utils.PageUtils;
import com.fission.machine.generator.utils.Query;
import com.fission.machine.generator.utils.R;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 代码生成器.
 *
 * @author devil
 */
@Controller
@RequestMapping("/sys/generator")
public class SysGeneratorController {
    
    @Autowired
    private SysGeneratorService sysGeneratorService;
    
    /**
     * 列表.
     */
    @ResponseBody
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils pageUtil = sysGeneratorService.queryList(new Query(params));
        
        return R.ok().put("page", pageUtil);
    }
    
    /**
     * 生成代码.
     */
    @RequestMapping("/code")
    public void code(String tables, HttpServletResponse response) throws IOException {
        byte[] data = sysGeneratorService.generatorCode(tables.split(","));
        
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"fission-machine-generator.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        
        IOUtils.write(data, response.getOutputStream());
    }
}
