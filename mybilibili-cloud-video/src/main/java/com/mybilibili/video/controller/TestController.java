package com.mybilibili.video.controller;

import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Resource
    private ServletWebServerApplicationContext servletWebServerApplicationContext;
    @RequestMapping("/test")
    public String test()
    {
        return "successfully" + servletWebServerApplicationContext.getWebServer().getPort();
    }

    @RequestMapping("/loadFeign")
    public String loadFeign()
    {
        return "来自video的loadFeign方法";
    }
}
