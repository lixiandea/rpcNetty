package com.lixiande.test;

import com.lixiande.server.NettyRPC;
import com.lixiande.server.NettyServer;
import com.lixiande.test.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcServerBootstrap {
    static final Logger logger = LoggerFactory.getLogger(RpcServerBootstrap.class);
    public static void main(String[] args) {
        //new ClassPathXmlApplicationContext("server-spring.xml");
        String serverAddress = "127.0.0.1";
        String registryAddress = "127.0.0.1:2181";
        int serverPort = 18877;
        //String registryAddress = "10.217.59.164:2181";
        NettyServer rpcServer = new NettyRPC(serverAddress, registryAddress, serverPort);
        HelloService helloService1 = new HelloServiceImpl();
        rpcServer.addService(HelloService.class.getName(), "1.0", helloService1);
        HelloService helloService2 = new HelloServiceImpl2();
        rpcServer.addService(HelloService.class.getName(), "2.0", helloService2);
        PersonService personService = new PersonServiceImpl();
        rpcServer.addService(PersonService.class.getName(), "", personService);
        try {
            rpcServer.start();
        } catch (Exception ex) {
            logger.error("Exception: {}", ex);
        }
    }
}
