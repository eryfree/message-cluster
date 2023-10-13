package com.erybobo.message.core.config;

import com.erybobo.message.core.vertical.SockJsBridgeVertical;
import com.erybobo.message.core.vertical.EventBridgeVertical;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import lombok.extern.log4j.Log4j2;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.jdbc.TcpDiscoveryJdbcIpFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
@Component
@EnableConfigurationProperties({IgniteClusterConfigProperties.class,DataSourceProperties.class})
@Log4j2
public class ClusterRunner implements CommandLineRunner {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Autowired
    private IgniteClusterConfigProperties igniteClusterConfigProperties;

    @Autowired
    private NacosConfig nacosConfig;

    @Autowired
    private NacosConfigV2 nacosConfigV2;

    private String deployId;

    private Vertx clusteredVertx;

    private IgniteConfig igniteConfig;


    public static void main(String[] args) throws Throwable {
        /*TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

        ipFinder.setMulticastGroup("228.10.10.157");

        spi.setIpFinder(ipFinder);
*/
        TcpDiscoverySpi spi = new TcpDiscoverySpi();

// Configure your DataSource.
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPassword("123456");
        hikariConfig.setUsername("root");
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/jqc?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true");


        DataSource someDs = new HikariDataSource(hikariConfig);


        TcpDiscoveryJdbcIpFinder ipFinder = new TcpDiscoveryJdbcIpFinder();

        ipFinder.setDataSource(someDs);

        spi.setIpFinder(ipFinder);
        spi.setLocalPort(48100);

        IgniteConfiguration cfg = new IgniteConfiguration();

// Override default discovery SPI.
        cfg.setDiscoverySpi(spi);


        Ignite ignite = Ignition.start(cfg);

        IgniteClusterManager igniteClusterManager = new IgniteClusterManager(ignite);

        VertxOptions vertxOptions = new VertxOptions().setClusterManager(igniteClusterManager);
        //EventBusOptions eventBusOptions = new EventBusOptions().setHost("0.0.0.0").setPort(10001);
        //vertxOptions.setEventBusOptions(eventBusOptions);
        Vertx.clusteredVertx(vertxOptions)
                .compose(vertx -> {
                            EventBus eventBus = vertx.eventBus();
                            eventBus.consumer("test.123", message -> {
                                System.out.println("收到的消息:" + message.body());
                            });
                            return vertx.deployVerticle(new EventBridgeVertical(vertx,10001,null,null));
                        }
                )
                .onSuccess(System.out::println)
                .onFailure(failure ->
                        Runtime.getRuntime().exit(0)
                );

    }

    @Override
    public void run(String... args) {


        SimpleCommandLinePropertySource simpleCommandLinePropertySource = new SimpleCommandLinePropertySource(args);

        IgniteClusterConfigResolve.convertProperties(simpleCommandLinePropertySource,igniteClusterConfigProperties);

        this.igniteConfig = new IgniteConfig(igniteClusterConfigProperties,dataSourceProperties);

        this.igniteConfig.init();

        IgniteClusterManager igniteClusterManager = new IgniteClusterManager(igniteConfig.getIgnite());


        VertxOptions vertxOptions = new VertxOptions().setClusterManager(igniteClusterManager);
        vertxOptions.setPreferNativeTransport(true);
        vertxOptions.setWorkerPoolSize(100);
        vertxOptions.setHAEnabled(true);
        vertxOptions.setHAGroup("message-service");
        vertxOptions.setMetricsOptions(
                new MicrometerMetricsOptions()
                        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                        .setEnabled(true));
        Vertx.clusteredVertx(vertxOptions)
                .compose(vertx -> {
                            boolean usingNative = vertx.isNativeTransportEnabled();
                            log.debug("usingNative:{}", () -> usingNative);
                            setClusteredVertx(vertx);
                            EventBus eventBus = vertx.eventBus();
                            eventBus.consumer("test.123", message -> {
                                log.info("收到的消息:{}", message::body);
                            });

                            NetServerOptions netServerOptions = new NetServerOptions();
                            DeploymentOptions deploymentOptions = new DeploymentOptions();
                            if (usingNative) {
                                netServerOptions.setReusePort(true);
                                netServerOptions.setPort(igniteClusterConfigProperties.getBridgePort());
                                deploymentOptions.setHa(true);
                                deploymentOptions.setInstances(5);
                                deploymentOptions.setWorker(true);
                            } else {
                                netServerOptions = null;
                            }
                            NetServerOptions finalNetServerOptions = netServerOptions;
                    Supplier<Verticle> tcpBridgeSupplier = () -> new EventBridgeVertical(vertx, igniteClusterConfigProperties.getBridgePort(), igniteClusterConfigProperties.getLocalhostStr(), finalNetServerOptions);
                    Supplier<Verticle> sockJsBridgeVerticalSupplier = () -> new SockJsBridgeVertical(vertx, igniteClusterConfigProperties.getBridgePort(), igniteClusterConfigProperties.getLocalhostStr(), null,null);
                            return vertx.deployVerticle(sockJsBridgeVerticalSupplier, deploymentOptions);
                        }
                )
                .compose(id -> {
                    this.setDeployId(id);
                    /*Promise<ImportedNacosService> promise = Promise.promise();
                    promise.complete(new ImportedNacosService("1",igniteClusterConfigProperties.getMulticastPort(),igniteClusterConfigProperties.getLocalhostStr()));
                    return promise.future();*/
                    return nacosConfigV2.registerInstance(igniteClusterConfigProperties.getLocalhostStr(), igniteClusterConfigProperties.getBridgePort());
                })
                .onSuccess(o -> log.debug("启动的ip:{}", o::getIp))
                .onFailure(failure -> {
                            log.error(() -> "启动失败", failure);
                            Runtime.getRuntime().exit(0);
                        }
                );

    }


    @PreDestroy
    public void destroy() {
        log.info(()->"Gracefully shutdown,开始关闭");
        Promise<Void> closePromise = Promise.promise();
        if (clusteredVertx != null) {
            nacosConfigV2.deRegisterInstance()
                    .compose(o-> clusteredVertx.close())
                    .compose(o-> this.igniteConfig.destroy())
                    .onSuccess(result -> closePromise.complete())
                    .onFailure(closePromise::fail);


            closePromise.future().onComplete(done -> {
                if (done.succeeded()) {
                    // 关闭完成，可以执行其他清理操作
                    clusteredVertx = null;
                    log.info(()->"Gracefully shutdown,关闭完毕");
                } else {
                    // 关闭失败，处理异常
                    log.error(()->"Failed to close clustered Vertx",done.cause());
                    // 处理异常，例如记录日志或抛出异常
                }
            });
        }
    }

    public String getDeployId() {
        return deployId;
    }

    public void setDeployId(String deployId) {
        this.deployId = deployId;
    }

    public Vertx getClusteredVertx() {
        return clusteredVertx;
    }

    public void setClusteredVertx(Vertx clusteredVertx) {
        this.clusteredVertx = clusteredVertx;
    }
}
