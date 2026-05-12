package com.mybilibili.admin.config;

import com.mybilibili.admin.interceptor.AdminLoginInterceptor;
import com.mybilibili.base.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private AdminLoginInterceptor adminLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/**")
                // 内部接口由服务间调用使用，不能要求浏览器侧的 adminToken。
                .excludePathPatterns(Constants.INNER_API_PREFIX + "/**");
    }
}
