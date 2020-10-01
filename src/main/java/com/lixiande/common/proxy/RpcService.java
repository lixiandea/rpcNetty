package com.lixiande.common.proxy;

import com.lixiande.client.RpcFuture;
/**
* @program: RpcService
*
* @description: service call interface
*
* @author: LiXiande
*
* @create: 16:42 2020/9/30
**/
public interface RpcService<T, P, FN extends SerializableFunction<T>> {
    RpcFuture call(String funcName, Object... args) throws Exception;
    RpcFuture call(FN fn, Object... args) throws Exception;
}
