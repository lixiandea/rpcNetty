package com.lixiande.common.codec;

import com.lixiande.common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RpcDecoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes()<4){
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        //System.out.println("------------------------------" + byteBuf.readableBytes()+"---------------------" );
        if(byteBuf.readableBytes()< dataLength){
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data =  new byte[dataLength];
        //byteBuf.writeBytes(data);
        byteBuf.readBytes(data);
        Object obj = null;
        try {
            obj = serializer.deserialize(data,genericClass);
            list.add(obj);
        }catch (Exception e){
            logger.error("Decode Error : " + e.toString());
        }
    }
}
