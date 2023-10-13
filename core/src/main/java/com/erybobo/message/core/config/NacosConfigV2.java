package com.erybobo.message.core.config;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.erybobo.message.core.discovery.ImportedNacosService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class NacosConfigV2 {

    @Autowired
    private NacosAutoServiceRegistration nacosAutoServiceRegistration;

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
            nacosAutoServiceRegistration.start();
            ImportedNacosService importedNacosService = ImportedNacosService.Builder.anImportedNacosService()
                    .withIp(ip)
                    .withPort(port)
                    .withServiceName("applicationName")
                    .build();
            this.setCurrentService(importedNacosService);
            promise.complete(importedNacosService);
        } catch (Exception e) {
            promise.fail(e);
        }
        return promise.future();
    }


    public Future<Boolean> deRegisterInstance() {
        Promise<Boolean> promise = Promise.promise();
        if (currentService != null) {
            try {
                nacosAutoServiceRegistration.stop();
                promise.complete(true);
            } catch (Exception e) {
                promise.fail(e);
            }
        } else {
            promise.fail("注册信息为空");
        }
        return promise.future();
    }
}
