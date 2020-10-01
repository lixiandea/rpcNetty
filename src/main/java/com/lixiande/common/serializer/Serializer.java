package com.lixiande.common.serializer;

/**
* @program: Serializer
*
* @description: Serializer implement
*
* @author: LiXiande
*
* @create: 16:47 2020/9/30
**/
public abstract class Serializer {
    /**
     *
     * @param obj
     * @param <T>
     * @return obj -> byte[] for socket transport
     */
    public abstract <T> byte[] serialize(T obj);

    /**
     *
     * @param bytes
     * @param clazz
     * @param <T>
     * @return byte[] -> obj for remote call
     */
    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
