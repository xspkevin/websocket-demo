package com.xsp.controller;

import com.xsp.service.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 测试
 * @Author: Xu Shengping
 * @Date: 2022/5/17 7:52 下午
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestServiceImpl testServiceImpl;

    /**
     * @Description: 启动页面
     * @Param: []
     * @Return: java.lang.String
     */
    @GetMapping("/start")
    public String start() {
        return "index";
    }

    @PostMapping("/pushToWeb")
    public String pushToWeb() {
        testServiceImpl.printTime();
        return "123456";
    }
}
