package com.lixiande.common.codec;

import com.lixiande.common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
* @program: RpcEncoder
*
* @description:
 * in this rpc application:
 * server
 *  |
 *  |registry service (rpcProtocol)
 *  v
 *  zookeeper<---discovery service-- client
 *
 *  and client send request to server with this procession:
 *  client->requst->Rpcencoder->server->Rpcdecoder->deserialize->invoke
 *  server -> response -> Rpcencoder -> client -> Rpcdecoder
 * call serializer.serialize() to generate byte stream
*
* @author: LiXiande
*
* @create: 15:22 2020/9/30
**/
public class RpcEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RpcEncoder(Class<?> genericClass, Serializer serializer){
        this.genericClass = genericClass;
        this.serializer = serializer;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(genericClass.isInstance(o)){
            try {
                byte[] data  = serializer.serialize(o);
                byteBuf.writeInt(data.length);
                byteBuf.writeBytes(data);
                //System.out.println("--------------"+byteBuf.readableBytes() + "--------------------");
            }catch (Exception e){
                logger.error("Encoder Error : " + e.toString());
            }
        }
    }
}
