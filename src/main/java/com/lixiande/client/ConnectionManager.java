package com.lixiande.client;

import com.lixiande.common.protocol.RpcProtocol;
import com.lixiande.common.protocol.RpcServiceInfo;
import com.lixiande.common.route.RpcLoadBalance;
import com.lixiande.common.route.impl.RocLoadBalanceRoundRobin;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionManager {
    public static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private EventLoopGroup group = new NioEventLoopGroup();
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4,8,600L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
    private Map<RpcProtocol, RpcClientHandler> connectServerNodes = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<RpcProtocol> rpcProtocols = new CopyOnWriteArraySet<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private RpcLoadBalance loadBalance = new RocLoadBalanceRoundRobin();
    private long waitTimeout = 5000;
    private volatile boolean isRunning = true;
    private ConnectionManager(){

    }
    private static class SingletonHolder{
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance(){
        return SingletonHolder.instance;
    }

    public void updateConnectServer(List<RpcProtocol> serviceList){
        if(serviceList!=null && serviceList.size()>0){
            HashSet<RpcProtocol> set = new HashSet<>(serviceList.size());
            for (int i = 0; i<serviceList.size(); ++i){
                RpcProtocol rpcProtocol = serviceList.get(i);
                set.add(rpcProtocol);
            }

            for(final RpcProtocol rpcProtocol: set){
                if(!rpcProtocols.contains(rpcProtocol)){
                    connectServerNodes(rpcProtocol);
                }
            }
        }
    }

    private void connectServerNodes(final RpcProtocol rpcProtocol){
        if(rpcProtocol.getServiceInfos() == null || rpcProtocol.getServiceInfos().isEmpty()){
            logger.info("No service on node, host : {}, port : {}",rpcProtocol.getHost(), rpcProtocol.getPort());
            return;
        }
        rpcProtocols.add(rpcProtocol);
        logger.info("New Service add , host:{}, port: {}",rpcProtocol.getHost(), rpcProtocol.getPort());
        for (RpcServiceInfo serviceInfo: rpcProtocol.getServiceInfos()){
            logger.info("new service info , name : {}, version: {}", serviceInfo.getServiceName(), serviceInfo.getVersion());
        }
        final InetSocketAddress remotePeer = new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group).channel(NioSocketChannel.class).handler(new RpcClientInitializer());
                ChannelFuture channelFuture = bootstrap.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            logger.info("Successfuly connect to remote server, remote peer = " + remotePeer);
                            RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            connectServerNodes.put(rpcProtocol, handler);
                            handler.setRpcProtocol(rpcProtocol);
                            signalAvailableHandler();
                        }else {
                            logger.error("Can not connect to remote server, remote peer = " + remotePeer);
                        }
                    }
                });
            }
        });
    }

    private void signalAvailableHandler(){
        lock.lock();
        try {
            connected.signalAll();
        }finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            logger.warn("waiting for available service ");
            return connected.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        }finally {
            lock.unlock();
        }
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int sz = connectServerNodes.values().size();
        while (isRunning && sz <= 0){
            try {
                waitingForHandler();
                sz = connectServerNodes.values().size();
            } catch (InterruptedException e) {
                logger.error("Waiting for available service is interrupted !", e);
            }
        }

        RpcProtocol protocol = loadBalance.route(serviceKey,connectServerNodes);
        RpcClientHandler handler = connectServerNodes.get(protocol);
        if(handler != null){
            return handler;
        }else {
            throw new Exception("can not get available connection");
        }
    }

    public void removeHandler(RpcProtocol protocol){
        rpcProtocols.remove(protocol);
        connectServerNodes.remove(protocol);
        logger.info("Remove one connection, host:{}, port : {}", protocol.getHost(), protocol.getPort());
    }

    public void stop(){
        isRunning = false;
        for(RpcProtocol protocol: rpcProtocols){
            RpcClientHandler handler = connectServerNodes.get(protocol);
            if(handler != null){
                handler.close();
            }
            connectServerNodes.remove(protocol);
            rpcProtocols.remove(protocol);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        group.shutdownGracefully();
    }



}
