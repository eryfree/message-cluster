package com.erybobo.message.autogonfigure;

import com.erybobo.message.core.config.IgniteClusterConfigProperties;
import com.erybobo.message.core.config.IgniteClusterConfigResolve;
import com.erybobo.message.core.config.IgniteConfig;
import com.erybobo.message.core.config.NacosConfig;
import com.erybobo.message.core.vertical.EventBridgeVertical;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import lombok.extern.log4j.Log4j2;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.jdbc.TcpDiscoveryJdbcIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.Collections;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
@Configuration
@EnableConfigurationProperties({IgniteClusterConfigProperties.class,DataSourceProperties.class})
@Log4j2
public class ClusterRunner implements CommandLineRunner {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Autowired
    private IgniteClusterConfigProperties igniteClusterConfigProperties;

    @Autowired
    private NacosConfig nacosConfig;

    private String deployId;

    private Vertx clusteredVertx;

    private IgniteConfig igniteConfig;

    private void localStartCluster () {

        IgniteConfiguration firstCfg = new IgniteConfiguration();

        firstCfg.setIgniteInstanceName("first");

// Explicitly configure TCP discovery SPI to provide list of initial nodes
// from the first cluster.
        TcpDiscoverySpi firstDiscoverySpi = new TcpDiscoverySpi();

// Initial local port to listen to.
        firstDiscoverySpi.setLocalPort(48501);

// Changing local port range. This is an optional action.
        firstDiscoverySpi.setLocalPortRange(20);

        TcpDiscoveryVmIpFinder firstIpFinder = new TcpDiscoveryVmIpFinder();

// Addresses and port range of the nodes from the first cluster.
// 127.0.0.1 can be replaced with actual IP addresses or host names.
// The port range is optional.
        firstIpFinder.setAddresses(Collections.singletonList("127.0.0.1:48500..48520"));

// Overriding IP finder.
        firstDiscoverySpi.setIpFinder(firstIpFinder);

// Explicitly configure TCP communication SPI by changing local port number for
// the nodes from the first cluster.
        TcpCommunicationSpi firstCommSpi = new TcpCommunicationSpi();

        firstCommSpi.setLocalPort(48100);

// Overriding discovery SPI.
        firstCfg.setDiscoverySpi(firstDiscoverySpi);

// Overriding communication SPI.
        firstCfg.setCommunicationSpi(firstCommSpi);

// Starting a node.
        Ignite ignite = Ignition.start(firstCfg);

        //Ignite ignite = Ignition.start(cfg);

        IgniteClusterManager igniteClusterManager = new IgniteClusterManager(ignite);

        VertxOptions vertxOptions = new VertxOptions().setClusterManager(igniteClusterManager);
        EventBusOptions eventBusOptions = new EventBusOptions().setHost("0.0.0.0").setPort(10001);
        vertxOptions.setEventBusOptions(eventBusOptions);
        Vertx.clusteredVertx(vertxOptions, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                EventBus eventBus = vertx.eventBus();
                eventBus.consumer("test.123",message -> {
                    System.out.println(message.body());
                });
                vertx.deployVerticle(new EventBridgeVertical(vertx,10001,null,null));
            } else {
                System.out.println(res.cause().getMessage());
            }
        });
    }

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
                            return vertx.deployVerticle(() -> new EventBridgeVertical(vertx, igniteClusterConfigProperties.getBridgePort(), igniteClusterConfigProperties.getLocalhostStr(), finalNetServerOptions), deploymentOptions);
                        }
                )
                .compose(id -> {
                    this.setDeployId(id);
                    return nacosConfig.registerInstance(igniteClusterConfigProperties.getLocalhostStr(), igniteClusterConfigProperties.getBridgePort());
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
            nacosConfig.deRegisterInstance()
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
