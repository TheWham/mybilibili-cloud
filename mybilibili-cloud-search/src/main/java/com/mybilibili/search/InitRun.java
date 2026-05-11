package com.mybilibili.search;

import com.mybilibili.search.component.EsVideoIndexInitializer;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author amani
 * 启动时初始化
 */
@Component
public class InitRun implements ApplicationRunner {

    @Resource
    private EsVideoIndexInitializer esVideoIndexInitializer;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        esVideoIndexInitializer.create();
    }
}
