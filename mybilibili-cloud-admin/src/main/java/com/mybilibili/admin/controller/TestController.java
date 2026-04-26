package com.mybilibili.admin.controller;

import com.mybilibili.api.feign.VideoClient;
import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Resource
    private VideoClient videoClient;
    @Resource
    private ServletWebServerApplicationContext servletWebServerApplicationContext;
    @RequestMapping("/test")
    public String test()
    {
      //  return "successfully" + servletWebServerApplicationContext.getWebServer().getPort();
        return videoClient.loadFeign();
    }


}
