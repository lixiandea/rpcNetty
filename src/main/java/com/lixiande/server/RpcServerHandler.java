package com.lixiande.server;

import com.lixiande.common.codec.Beat;
import com.lixiande.common.codec.RpcRequest;
import com.lixiande.common.codec.RpcResponse;
import com.lixiande.common.util.ServiceUtil;
import com.sun.xml.internal.bind.v2.model.core.ID;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
/**
* @program: RpcServerHandler
*
* @description: RpcServerHandler
*
* @author: LiXiande
*
* @create: 17:13 2020/9/30
**/
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    public static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    // serviceKey to Object
    private final Map<String, Object> handlerMap;
    // server handler pool
    private final ThreadPoolExecutor serverHandlerPool;

    public RpcServerHandler(Map<String, Object> handlerMap, ThreadPoolExecutor serverHandlerPool){
        this.handlerMap = handlerMap;
        this.serverHandlerPool = serverHandlerPool;
    }

    /**
     * solve request and do remote call
     * @param channelHandlerContext : channel context
     * @param rpcRequest : request contains remote call infos
     * @throws Exception
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final RpcRequest rpcRequest) throws Exception {
        // if request is heart beat： return
        if(Beat.BEAT_ID.equalsIgnoreCase(rpcRequest.getRequestId())){
            logger.info("Receive request read heartbeat ping");
            return;
        }
        serverHandlerPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Receive request " + rpcRequest.getRequestId());
                //generate response
                final RpcResponse response = new RpcResponse();
                response.setRequestId(rpcRequest.getRequestId());
                try{
                    Object result = handle(rpcRequest);
                } catch (InvocationTargetException e) {
                    response.setError(e.toString());
                    logger.error("RPC server handle request error" , e);
                }
                // return response
                channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        logger.info("Send response for request " + rpcRequest.getRequestId());
                    }
                });
            }
        });
    }

    // do handle request
    public Object handle(RpcRequest request) throws InvocationTargetException {
        String className = request.getClassName();
        String version = request.getVersion();
        String serviceKey = ServiceUtil.makeServiceKey(className, version);
        // deserialize service class and method
        Object serviceBean = handlerMap.get(serviceKey);
        if(serviceBean == null){
            logger.error("can't find service implement with interface name： {} and version: {}", className, version);
            return null;
        }
//        generate class instance and invoke
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        logger.debug(serviceClass.getName() + ": " + methodName);
        for(int i = 0; i< parameterTypes.length; i++){
            logger.debug(parameterTypes[i].getName());
        }
        for(int i = 0; i < parameters.length; ++i){
            logger.debug(parameters[i].toString());
        }
//        invoke method
        FastClass serviceFastClass = FastClass.create(serviceClass);
        int methodIndex = serviceFastClass.getIndex(methodName,parameterTypes);
        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception : " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
