package com.lixiande.common.proxy;

import com.lixiande.client.ConnectionManager;
import com.lixiande.client.RpcClient;
import com.lixiande.client.RpcClientHandler;
import com.lixiande.client.RpcFuture;
import com.lixiande.common.codec.RpcRequest;
import com.lixiande.common.util.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.ObjectError;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy<T, P> implements
        InvocationHandler, RpcService<T, P, SerializableFunction<T>> {
    public static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;
    private String version;

    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    @Override
    public RpcFuture call(String funcName, Object... args) throws Exception {
        String serviceKey = ServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = creatRequest(clazz.getName(), funcName, args);
        RpcFuture future = handler.sendRequest(request);
        return future;
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Object... args) throws Exception {
        String serviceKey = ServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = creatRequest(clazz.getName(), tSerializableFunction.getName(), args);
        RpcFuture future = handler.sendRequest(request);
        return future;
    }

    private RpcRequest creatRequest(String className, String methodName, Object[] args){
        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(version);
        Class[] parameterTypes = new Class[args.length];
        for(int i = 0; i< args.length; i++){
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
        logIfDebug(request);
        return request;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class == method.getDeclaringClass()){
            String name = method.getName();
            if("equals".equals(name)){
                return proxy == args[0];
            }else if("hashCode".equals(name)){
                return System.identityHashCode(proxy);
            }else if("toString".equals(name)){
                return proxy.getClass().getName()+"@"+
                        Integer.toHexString(System.identityHashCode(proxy)) +", with InvocationHandler " + this;
            }else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        logIfDebug(request);
        String serviceKey = ServiceUtil.makeServiceKey(method.getDeclaringClass().getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcFuture future = handler.sendRequest(request);
        return future.get();
    }

    private Class<?> getClassType(Object obj){
        Class<?> clazz = obj.getClass();
        return clazz;

    }

    private void logIfDebug(RpcRequest request){
        if(logger.isDebugEnabled()){
            logger.debug(request.getClassName());
            logger.debug(request.getMethodName());
            for(int i = 0; i< request.getParameterTypes().length; i++){
                logger.debug(request.getParameterTypes()[i].getName());
                logger.debug(request.getParameters()[i].toString());
            }
        }
    }

}
