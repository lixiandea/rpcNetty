package com.lixiande.common.proxy;

import com.lixiande.client.RpcFuture;

public interface RpcService<T, P, FN extends SerializableFunction<T>> {
    RpcFuture call(String funcName, Object... args) throws Exception;
    RpcFuture call(FN fn, Object... args) throws Exception;
}
