package com.icekredit.juzi.message.core.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.impl.VertxWrapper;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
@Component
@EnableConfigurationProperties(IgniteConfig.class)
public class ClusterRunner implements CommandLineRunner {

    public static void main(String[] args) {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

        ipFinder.setMulticastGroup("228.10.10.157");

        spi.setIpFinder(ipFinder);

        IgniteConfiguration cfg = new IgniteConfiguration();

// Override default discovery SPI.
        cfg.setDiscoverySpi(spi);

        Ignite ignite = Ignition.start(cfg);

        IgniteClusterManager igniteClusterManager = new IgniteClusterManager(ignite);

        VertxOptions vertxOptions = new VertxOptions().setClusterManager(igniteClusterManager);
        EventBusOptions eventBusOptions = new EventBusOptions().setHost("0.0.0.0").setPort(10001);
        vertxOptions.setEventBusOptions(eventBusOptions);
        Vertx.clusteredVertx(vertxOptions, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println(vertx.isClustered());
                EventBus eventBus = vertx.eventBus();
                eventBus.consumer("test.123",message -> {
                    System.out.println(message.body());
                });
                eventBus.send("test.123","hello world");
            } else {
                System.out.println(res.cause().getMessage());
            }
        });

    }

    @Override
    public void run(String... args) throws Exception {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

        ipFinder.setMulticastGroup("228.10.10.157");

        spi.setIpFinder(ipFinder);

        IgniteConfiguration cfg = new IgniteConfiguration();

        cfg.setDiscoverySpi(spi);

        Ignite ignite = Ignition.start(cfg);

        IgniteClusterManager igniteClusterManager = new IgniteClusterManager(ignite);

        VertxOptions vertxOptions = new VertxOptions()
                .setClusterManager(igniteClusterManager);
    }
}
