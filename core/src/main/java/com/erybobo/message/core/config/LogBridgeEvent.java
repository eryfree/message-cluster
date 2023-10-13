package com.erybobo.message.core.config;

import io.vertx.core.Handler;
import io.vertx.ext.eventbus.bridge.tcp.BridgeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2023/8/30.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class LogBridgeEvent implements Handler<BridgeEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LogBridgeEvent.class);
    /**
     * Something has happened, so handle it.
     *
     * @param event the event to handle
     */
    @Override
    public void handle(BridgeEvent event) {
        try {
            logger.info("bridge收到Event:{}", event.getRawMessage());
            event.complete(true);
        } catch (Exception e) {
            event.fail(e);
        }
    }
}
