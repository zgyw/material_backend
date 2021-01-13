package com.zgyw.materiel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:sftp.properties")
@ConfigurationProperties(prefix="sftp.client")
public class SftpConfig {
    private String host;

    private Integer port;

    private String protocol;

    private String username;

    private String password;

    private String root;

    private String sessionStrictHostKeyChecking;

    private Integer sessionConnectTimeout;

    private Integer channelConnectedTimeout;
}
