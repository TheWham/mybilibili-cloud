package com.mybilibili.user.controller;

import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@LoginInterceptor(checkLogin = true)
@RequestMapping("/ucenter")
public class UCenterController extends ABaseController {

}
