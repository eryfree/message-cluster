package com.erybobo.message.core.vertical;

import com.erybobo.message.core.config.SockJsLogBridgeEvent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
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
public class SockJsBridgeVertical extends AbstractVerticle {

    private SockJSHandler sockJSHandler;
    private Integer bridgePort;
    private String ip;
    private SockJSBridgeOptions bridgeOptions;
    private SockJSHandlerOptions sockJSHandlerOptions;
    private HttpServer httpServer;


    public SockJsBridgeVertical(Vertx vertx, Integer bridgePort, String ip, SockJSBridgeOptions bridgeOptions,SockJSHandlerOptions sockJSHandlerOptions) {
        this.bridgeOptions = bridgeOptions == null ? new SockJSBridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("hello"))
                .addInboundPermitted(new PermittedOptions().setAddress("echo"))
                .addInboundPermitted(new PermittedOptions().setAddress("test.123"))
                .addInboundPermitted(new PermittedOptions().setAddress("echo"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("^[\\w\\.]+$")) : bridgeOptions;
        this.sockJSHandlerOptions = sockJSHandlerOptions == null ? new SockJSHandlerOptions() : sockJSHandlerOptions;
        this.sockJSHandler = SockJSHandler.create(vertx,this.sockJSHandlerOptions);
        this.bridgePort = bridgePort;
        this.ip = ip;
    }


    /**
     * If your verticle does a simple, synchronous start-up then override this method and put your start-up
     * code in here.
     *
     */
    @Override
    public void start(Promise<Void> startPromise) {
        this.httpServer = getVertx().createHttpServer();
        Router routerMetrics = Router.router(getVertx());
        routerMetrics.route("/metrics").handler(PrometheusScrapingHandler.create());
        //vertx.createHttpServer().requestHandler(router).listen(8080);
        Router router = Router.router(getVertx());

        router.route("/eventbus/*")
                .subRouter(sockJSHandler.bridge(this.bridgeOptions,new SockJsLogBridgeEvent()));

        if (StringUtils.isEmpty(ip)) {

            this.httpServer.requestHandler(router)
                    .requestHandler(routerMetrics).listen(bridgePort).onSuccess(event -> {
                        log.info("HttpServer bridge started");
                        startPromise.complete();
                    }
            ).onFailure(event -> {
                        log.error("HttpServer bridge failed to start", event);
                        startPromise.fail(event);
                    }
            );
        } else {
            this.httpServer.requestHandler(router)
                    .requestHandler(routerMetrics).listen(bridgePort, ip).onSuccess(event ->{
                        log.info("HttpServer bridge started");
                        startPromise.complete();
                    }
            ).onFailure(event -> {
                        log.error("HttpServer bridge failed to start", event);
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
        this.httpServer.close(ar->{
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
