package com.lixiande.client;

import com.lixiande.common.codec.Beat;
import com.lixiande.common.codec.RpcRequest;
import com.lixiande.common.codec.RpcResponse;
import com.lixiande.common.protocol.RpcProtocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
@Data
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    public static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    private volatile Channel channel;
    private SocketAddress remotePeer;
    private RpcProtocol rpcProtocol;

    private ConcurrentHashMap<String, RpcFuture> pendingRpc = new ConcurrentHashMap<>();
    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * send request and get invoke result
     * @param request : request to send
     * @return
     */
    public RpcFuture sendRequest(RpcRequest request){
        RpcFuture future = new RpcFuture(request);
        pendingRpc.put(request.getRequestId(), future);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if(channelFuture.isSuccess()){
                logger.info("Send request {} success", request.getRequestId());
            } else {
                logger.error("Send request {} error", request.getRequestId());
            }
        }catch (InterruptedException e){
            logger.error("send request exception : " + e.getMessage());
        }
        return future;
    }


    /**
     * deal channel read context
     * @param channelHandlerContext : channel context
     * @param rpcResponse : for client, all need is to deal with response
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        logger.debug("response receive: " +requestId);
        RpcFuture rpcFuture =pendingRpc.get(requestId);
        if(rpcFuture!= null){
            pendingRpc.remove(requestId);
            rpcFuture.done(rpcResponse);
        }else {
            logger.warn("can not get pending response for request id: " + requestId);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionManager.getInstance().removeHandler(rpcProtocol);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //Send ping
            sendRequest(Beat.BEAT_PING);
            logger.debug("Client send beat-ping to " + remotePeer);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

}
