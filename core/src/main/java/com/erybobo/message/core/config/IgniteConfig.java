package com.erybobo.message.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.spi.cluster.ignite.impl.VertxLogger;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.jdbc.TcpDiscoveryJdbcIpFinder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Created on 2023/9/20.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class IgniteConfig {


    private Ignite ignite;


    private HikariDataSource dataSource;

    private IgniteClusterConfigProperties igniteClusterConfigProperties;


    private DataSourceProperties dataSourceProperties;


    public IgniteConfig(IgniteClusterConfigProperties igniteClusterConfigProperties, DataSourceProperties dataSourceProperties) {
        this.igniteClusterConfigProperties = igniteClusterConfigProperties;
        this.dataSourceProperties = dataSourceProperties;
    }

    public void init () {

        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryJdbcIpFinder ipFinder = new TcpDiscoveryJdbcIpFinder();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPassword(dataSourceProperties.getPassword());
        hikariConfig.setUsername(dataSourceProperties.getUsername());
        hikariConfig.setDriverClassName(dataSourceProperties.getDriverClassName());
        hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
        hikariConfig.setPoolName("ignite-discovery-pool");
        this.dataSource = new HikariDataSource(hikariConfig);

        ipFinder.setDataSource(dataSource);

        spi.setIpFinder(ipFinder);
        spi.setLocalAddress(igniteClusterConfigProperties.getLocalhostStr());
        spi.setLocalPort(igniteClusterConfigProperties.getMulticastPort());

        IgniteConfiguration cfg = new IgniteConfiguration();
        CacheConfiguration<Object,Object> cache1 = new CacheConfiguration<>();
        cache1.setName("__vertx.*");
        cache1.setCacheMode(CacheMode.REPLICATED);
        cache1.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cache1.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        CacheConfiguration<Object,Object>  cache2 = new CacheConfiguration<>();
        cache2.setName("*");
        cache2.setCacheMode(CacheMode.PARTITIONED);
        cache2.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        cache2.setBackups(1);
        cache2.setReadFromBackup(false);
        cache2.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        cache2.setAffinity(new RendezvousAffinityFunction(false, 128));

        cfg.setGridLogger(new VertxLogger());
        cfg.setCacheConfiguration(cache1,cache2);
        cfg.setWorkDirectory(igniteClusterConfigProperties.getWorkDir());
        cfg.setMetricsLogFrequency(0);
        // 设置spi
        cfg.setDiscoverySpi(spi);
        this.ignite = Ignition.start(cfg);
    }

    public Ignite getIgnite() {
        return ignite;
    }

    public void setIgnite(Ignite ignite) {
        this.ignite = ignite;
    }


    public Future<Boolean> destroy() {
        Promise<Boolean> promise = Promise.promise();
        try {
            this.ignite.close();
            HikariDataSource hikariDataSource = this.dataSource;
            hikariDataSource.close();
            promise.complete(true);
        } catch (Exception e) {
            promise.fail(e);
        }
        return promise.future();
    }
}
