package com.erybobo.message.eventbridge;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import io.vertx.core.json.JsonObject;
import io.vertx.eventbusclient.EventBusClient;
import io.vertx.eventbusclient.EventBusClientOptions;
import io.vertx.eventbusclient.Handler;
import io.vertx.eventbusclient.json.JacksonCodec;
import io.vertx.eventbusclient.json.JsonCodec;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2023/8/28.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class TestBridge {

    public static void main(String[] args) throws NacosException {
        NamingService naming = NamingFactory.createNamingService("172.22.4.253:8848");

        naming.subscribe("message-cluster",event -> {
            if (event instanceof NamingEvent) {
                NamingEvent e = (NamingEvent) event;
                System.out.println(e.getInstances());
            }
        });

        EventBusClientOptions options = new EventBusClientOptions()
                .setHost("172.22.4.253").setPort(10001);
                //.setSsl(true)
                //.setTrustStorePath("/path/to/store.jks")
                //.setTrustStorePassword("change-it");
        EventBusClient sslTcpEventBusClient = EventBusClient.webSocket(options,new JacksonCodec());
        sslTcpEventBusClient.connect()
                .closeHandler(closeHandler-> sslTcpEventBusClient.connect())
                .connectedHandler(
                        handler -> {
                            System.out.println("connected");
                            // 这个handler的调用非常重要如果后端断开了，重连这个是需要把所有的consumer地址都重新register一遍的
                            handler.handle(null);
                            if (sslTcpEventBusClient.isConnected()) {
                                sslTcpEventBusClient.consumer("echo", message -> {
                                    System.out.println(message.body());
                                    message.reply(new JsonObject().put("reply", message.body().toString()));
                                });
                            }
                        }
                )
                .exceptionHandler(
                        event -> System.out.println("exception"+event.getMessage()));



        /*for (int i = 0; i < 100000; i++) {
            sslTcpEventBusClient.send("test.123", new JsonObject().put("name", i));
        }*/

    }
}
