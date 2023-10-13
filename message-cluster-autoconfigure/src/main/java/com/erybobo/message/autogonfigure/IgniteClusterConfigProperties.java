package com.erybobo.message.autogonfigure;

import cn.hutool.core.net.NetUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
@ConfigurationProperties(prefix = "cluster")
public class IgniteClusterConfigProperties {

    private static final String DEFAULT_WORK_DIR = "/data/ignite/work/";
    private Integer multicastPort;
    private Integer bridgePort;
    private String workDir = DEFAULT_WORK_DIR;
    private String localhostStr = NetUtil.getLocalhostStr();

    public Integer getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(Integer multicastPort) {
        this.multicastPort = multicastPort;
    }

    public Integer getBridgePort() {
        return bridgePort;
    }

    public void setBridgePort(Integer bridgePort) {
        this.bridgePort = bridgePort;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getLocalhostStr() {
        return localhostStr;
    }

    public void setLocalhostStr(String localhostStr) {
        this.localhostStr = localhostStr;
    }
}
