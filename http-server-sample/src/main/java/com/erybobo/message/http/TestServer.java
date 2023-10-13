package com.erybobo.message.http;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.eventbusclient.EventBusClient;
import io.vertx.eventbusclient.EventBusClientOptions;
import io.vertx.eventbusclient.json.JacksonCodec;

/**
 * Created on 2023/9/18.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class TestServer {


    public static void main(String[] args) {
        System.out.println("Hello World!");

        EventBusClientOptions options1 = new EventBusClientOptions()
                .setHost("172.22.4.253").setPort(10001);
        EventBusClient client1 = EventBusClient.webSocket(options1,new JacksonCodec());

       /* client1.consumer("echo", event -> {
                    System.out.println(event.body());
                }
        );*/
        for (int i = 0; i < 10; i++) {
            client1.request("echo", new JsonObject().put("name", i),reply->{
                System.out.println(reply.result().body());
            });
        }

    }
}
