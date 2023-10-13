package com.erybobo.message.core.vertical;

import com.erybobo.message.core.config.LogBridgeEvent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServerOptions;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
@Log4j2
public class EventBridgeVertical extends AbstractVerticle {

    private TcpEventBusBridge bridge;
    private Integer bridgePort;
    private String ip;
    private BridgeOptions bridgeOptions;


    public EventBridgeVertical(Vertx vertx, Integer bridgePort, String ip, NetServerOptions netServerOptions) {
        this.bridgeOptions = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("hello"))
                .addInboundPermitted(new PermittedOptions().setAddress("echo"))
                .addInboundPermitted(new PermittedOptions().setAddress("test.123"))
                .addInboundPermitted(new PermittedOptions().setAddress("echo"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("^[\\w\\.]+$"));
        this.bridge = TcpEventBusBridge.create(
                vertx,bridgeOptions
                ,netServerOptions, new LogBridgeEvent());
        this.bridgePort = bridgePort;
        this.ip = ip;
    }


    public EventBridgeVertical(TcpEventBusBridge bridge) {
        this.bridge = bridge;
    }


    /**
     * If your verticle does a simple, synchronous start-up then override this method and put your start-up
     * code in here.
     *
     */
    @Override
    public void start(Promise<Void> startPromise) {

        if (StringUtils.isEmpty(ip)) {
            bridge.listen(bridgePort).onSuccess(event -> {
                        log.info("TCP bridge started");
                        startPromise.complete();
                    }
            ).onFailure(event -> {
                        log.error("TCP bridge failed to start", event);
                        startPromise.fail(event);
                    }
            );
        } else {
            bridge.listen(bridgePort, ip).onSuccess(event ->{
                        log.info("TCP bridge started");
                        startPromise.complete();
                    }
            ).onFailure(event -> {
                        log.error("TCP bridge failed to start", event);
                        startPromise.fail(event);
                    }
            );
        }
    }

    /**
     * If your verticle has simple synchronous clean-up tasks to complete then override this method and put your clean-up
     * code in here.
     *
     * @throws Exception
     */
    @Override
    public void stop() {
        bridge.close(ar->{
            if (ar.succeeded()) {
                log.info("TCP bridge closed");
            } else {
                log.error(()->"TCP bridge closed error", ar.cause());
            }
        });
    }

    public BridgeOptions getBridgeOptions() {
        return bridgeOptions;
    }
}
