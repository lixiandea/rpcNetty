package com.lixiande.common.serializer.kryo;

import com.lixiande.common.serializer.Serializer;

/**
* @program: KryoSerializer
*
* @description: kryo Serializer
*
* @author: LiXiande
*
* @create: 16:49 2020/9/30
**/
public class KryoSerializer extends Serializer {
//TODO: kryo


    @Override
    public <T> byte[] serialize(T obj) {
        return new byte[0];
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
