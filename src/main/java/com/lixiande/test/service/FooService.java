package com.lixiande.test.service;

import com.lixiande.common.annotation.RpcAutowired;

public class FooService implements Foo{
    @RpcAutowired(version = "1.0")
    private HelloService helloService1;

    @RpcAutowired(version = "2.0")
    private HelloService helloService2;


    @Override
    public String say(String s) {
        return helloService1.hello(s);
    }
}
