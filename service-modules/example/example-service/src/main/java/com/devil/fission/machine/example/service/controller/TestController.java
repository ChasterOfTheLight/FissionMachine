package com.devil.fission.machine.example.service.controller;

import com.devil.fission.machine.common.response.Response;
import com.devil.fission.machine.example.service.utils.NoGenUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController.
 *
 * @author Devil
 * @date Created in 2024/6/21 下午3:22
 */
@RequestMapping(value = "/test")
@RestController
public class TestController {
    
    private final NoGenUtils noGenUtils;
    
    public TestController(NoGenUtils noGenUtils) {
        this.noGenUtils = noGenUtils;
    }
    
    @PostMapping(value = "/genOrderNo")
    public Response<String> genOrderNo() {
        return Response.success(noGenUtils.genOrderNo(1));
    }
}
