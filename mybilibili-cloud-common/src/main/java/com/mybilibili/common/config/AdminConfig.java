package com.mybilibili.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AdminConfig {

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:}")
    private String account;
    @Value("${admin.password:}")
    private String password;

    @Value("${showFFmpegLog:true}")
    private Boolean showFFmpegLog;

    @Value("${es.index.video.name:easylive_video}")
    private String esIndexVideoName;

    @Value("${es.host.port:127.0.0.1:9201}")
    private String esHostPort;

    public String getEsIndexVideoName() {
        return esIndexVideoName;
    }

    public String getEsHostPort() {
        return esHostPort;
    }

    public Boolean getShowFFmpegLog() {
        return showFFmpegLog;
    }

    public String getAccount() {
        return account;
    }


    public String getPassword() {
        return password;
    }

    public String getProjectFolder() {
        return projectFolder;
    }
}
