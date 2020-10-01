package com.lixiande.common.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
* @program: SerializableFunction
*
* @description: method serializable
*
* @author: LiXiande
*
* @create: 16:43 2020/9/30
**/
public interface SerializableFunction<T> extends Serializable {
    default String getName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method write = this.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) write.invoke(this);
        return serializedLambda.getImplMethodName();
    }
}
