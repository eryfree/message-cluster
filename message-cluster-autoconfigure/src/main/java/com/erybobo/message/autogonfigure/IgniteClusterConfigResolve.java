package com.erybobo.message.autogonfigure;

import org.springframework.core.env.SimpleCommandLinePropertySource;

/**
 * Created on 2023/9/20.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class IgniteClusterConfigResolve {

    private  IgniteClusterConfigResolve() {

    }


    public static void convertProperties (SimpleCommandLinePropertySource simpleCommandLinePropertySource, IgniteClusterConfigProperties igniteClusterConfigProperties) {
        try {
            String multicastPort = simpleCommandLinePropertySource.getProperty("cluster.multicast-port");
            if (multicastPort != null) {
                Integer port = Integer.parseInt(multicastPort);
                igniteClusterConfigProperties.setMulticastPort(port);
            }
            String bridgePort = simpleCommandLinePropertySource.getProperty("cluster.bridge-port");
            if (bridgePort != null) {
                Integer port = Integer.parseInt(bridgePort);
                igniteClusterConfigProperties.setBridgePort(port);
            }
            String workDir = simpleCommandLinePropertySource.getProperty("cluster.work-dir");
            if (workDir != null) {
                igniteClusterConfigProperties.setWorkDir(workDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("组播端口和桥接端口必须为数字");
        }
    }
}


