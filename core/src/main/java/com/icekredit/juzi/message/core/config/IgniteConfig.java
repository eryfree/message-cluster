package com.icekredit.juzi.message.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
@ConfigurationProperties(prefix = "icekredit.juzi.message")
public class IgniteConfig {
    private String multicastGroup;
    private String port;

    public String getMulticastGroup() {
        return multicastGroup;
    }

    public void setMulticastGroup(String multicastGroup) {
        this.multicastGroup = multicastGroup;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
