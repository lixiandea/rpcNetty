package com.lixiande.client;

import com.lixiande.common.annotation.RpcAutowired;
import com.lixiande.common.proxy.ObjectProxy;
import com.lixiande.common.proxy.RpcService;
import com.lixiande.common.service.discovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient implements ApplicationContextAware, DisposableBean {
    public static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private ServiceDiscovery discovery;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));

    public RpcClient(ServiceDiscovery discovery) {
        this.discovery = discovery;
    }

    public static <T,P> T createService(Class<T> interfaceClass, String version){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T, P>(interfaceClass,version));
    }

    public static <T, P> RpcService createAsyncService(Class<T> interfaceClass, String version){
        return new ObjectProxy<T, P> (interfaceClass, version);
    }




    @Override
    public void destroy() throws Exception {
        this.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : beanNames){
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try {
                for(Field field : fields){
                    RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
                    if(rpcAutowired != null){
                        String version = rpcAutowired.version();
                        field.setAccessible(true);
                        field.set(bean, createService(field.getType(), version));
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(e.toString());
            }

        }
    }

    public void stop(){
        threadPoolExecutor.shutdown();
        discovery.stop();
    }
    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }
}
