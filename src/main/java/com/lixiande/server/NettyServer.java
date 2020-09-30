package com.lixiande.server;


import com.lixiande.common.util.ServiceUtil;
import com.lixiande.common.util.ThreadPoolUtil;
import com.lixiande.common.zookeeper.CuratorClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class NettyServer implements Server{
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Thread thread;

    private String serverAddrress;
    private int port;

    private ServiceRegistry serviceRegistry;
    private Map<String , Object> serviceMap = new HashMap<>();

    //static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));

    public NettyServer(String serverAddrress, int port, String registryAddress) {
        this.serverAddrress = serverAddrress;
        this.port = port;
        this.serviceRegistry = new ServiceRegistry(new CuratorClient(registryAddress, 5000));
    }

/**
* @program: NettyServer
*
* @description: start server for with netty
*
* @author: LiXiande
*
* @create: 0:04 2020/9/27 
**/
    public void start() {
        thread = new Thread(new Runnable() {
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServiceThreadPool(NettyServer.class.getSimpleName(),4,8);
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerInitializer(serviceMap,threadPoolExecutor))
                            .option(ChannelOption.SO_BACKLOG,128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
                    ChannelFuture future = bootstrap.bind(serverAddrress,port).sync();
                    if(serviceRegistry != null){
                        serviceRegistry.registryService(serverAddrress,port,serviceMap);
                    }
                    logger.info("Server started on {}: {} ", serverAddrress,port);
                    future.channel().closeFuture().sync();
                }catch (Exception e){
                    if(e instanceof InterruptedException){
                        logger.error("rpc Server remoting server stop");
                    }else {
                        logger.error("rpc Server remoting server error ", e);
                    }

                }finally {
                    try {
                        serviceRegistry.unregisterService();
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    }catch (Exception e){
                        logger.error(e.getMessage(), e);
                    }
                }

            }
        });
        thread.start();
    }
    /**
    * @program: NettyServer
    *
    * @description: stop server
    *
    * @author: LiXiande
    *
    * @create: 0:03 2020/9/27
    **/
    public void stop() {
        if (thread!=null && thread.isAlive()){
            thread.interrupt();
        }
    }


    public void addService(String interfaceName, String version, Object serviceBean){
        logger.info("adding service, interface : {}, version: {}, bean : {}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }
}
