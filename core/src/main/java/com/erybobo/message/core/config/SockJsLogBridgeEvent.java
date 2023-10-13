package com.erybobo.message.core.config;

import io.vertx.core.Handler;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class SockJsLogBridgeEvent implements Handler<BridgeEvent> {

    /**
     * Something has happened, so handle it.
     *
     * @param event the event to handle
     */
    @Override
    public void handle(BridgeEvent event) {
        try {
            SockJSSocket socket = event.socket();
            if (event.type() != BridgeEventType.SOCKET_PING && socket != null) {
                log.info("bridge收到Event,uri:{},type:{},content:{}", socket::uri, event::type, event::getRawMessage);
                event.complete(true);
            }
        } catch (Exception e) {
            event.fail(e);
        }
    }
}
