package com.lixiande.common.codec;

import com.lixiande.common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
