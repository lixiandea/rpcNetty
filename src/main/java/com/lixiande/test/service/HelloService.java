package com.lixiande.test.service;

public interface HelloService {
    String hello(String name);

    String hello(Person person);

    String hello(String name, Integer age);
}
