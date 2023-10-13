package com.erybobo.message.core.utils;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Created on 2023/10/11.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class MutinyTest {


    public static void main(String[] args) {
        Uni<String> uni1 = Uni.createFrom().item("hello");
        Uni<String> uni2 = uni1.onItem().transform(item -> item + " mutiny");
        Uni<String> uni3 = uni2.onItem().transform(String::toUpperCase);

        uni3.subscribe().with(item -> System.out.println(">> " + item));
        uni1.subscribe().with(item -> System.out.println(">> " + item));

        Multi.createFrom().items(1, 2, 3, 4, 5)
                .onItem().transform(i -> i * 2)
                .select().first(3)
                .onFailure().recoverWithItem(0)
                .subscribe().with(System.out::println);
    }

}
