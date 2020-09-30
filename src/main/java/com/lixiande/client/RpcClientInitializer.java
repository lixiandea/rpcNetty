package com.lixiande.client;

import com.lixiande.common.codec.*;
import com.lixiande.common.serializer.Serializer;
import com.lixiande.common.serializer.hessian.HessianSerializer;
import com.lixiande.common.serializer.kryo.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        Serializer serializer = HessianSerializer.class.newInstance();
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        pipeline.addLast(new RpcEncoder(RpcRequest.class, serializer));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0,4,0,0));
        pipeline.addLast(new RpcDecoder(RpcResponse.class, serializer));
        pipeline.addLast(new RpcClientHandler());
    }
}
