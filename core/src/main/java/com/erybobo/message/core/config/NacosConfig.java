package com.erybobo.message.core.config;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.erybobo.message.core.discovery.ImportedNacosService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2023/8/31.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
//@ConditionalOnProperty(name = NacosDiscoveryConstants.ENABLED, matchIfMissing = true)
//@ConditionalOnMissingBean(name = DISCOVERY_GLOBAL_NACOS_PROPERTIES_BEAN_NAME)
//@EnableConfigurationProperties(value = NacosDiscoveryProperties.class)
@Configuration
@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${nacos.server-addr}", enableRemoteSyncConfig = "true", maxRetry = "5", configRetryTime = "4000", configLongPollTimeout = "26000", username = "nacos", password = "nacos"))
public class NacosConfig {

    @NacosInjected
    private NamingService namingService;


    @Value("${spring.application.name}")
    private String applicationName;

    private ImportedNacosService currentService;


    public ImportedNacosService getCurrentService() {
        return currentService;
    }

    public void setCurrentService(ImportedNacosService currentService) {
        this.currentService = currentService;
    }


    public Future<ImportedNacosService> registerInstance(String ip, Integer port) {
        Promise<ImportedNacosService> promise = Promise.promise();
        try {
            namingService.registerInstance(applicationName, ip, port);
            ImportedNacosService importedNacosService = ImportedNacosService.Builder.anImportedNacosService()
                    .withIp(ip)
                    .withPort(port)
                    .withServiceName(applicationName)
                    .build();
            this.setCurrentService(importedNacosService);
            promise.complete(importedNacosService);
        } catch (NacosException e) {
            promise.fail(e);
        }
        return promise.future();
    }


    public Future<Boolean> deRegisterInstance() {
        Promise<Boolean> promise = Promise.promise();
        if (currentService != null) {
            try {
                namingService.deregisterInstance(applicationName, currentService.getIp(), currentService.getPort());
                promise.complete(true);
            } catch (NacosException e) {
                promise.fail(e);
            }
        } else {
            promise.fail("注册信息为空");
        }
        return promise.future();
    }
}
