package com.lixiande.server;

import com.lixiande.common.config.Constant;
import com.lixiande.common.protocol.RpcProtocol;
import com.lixiande.common.protocol.RpcServiceInfo;
import com.lixiande.common.util.ServiceUtil;
import com.lixiande.common.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    private CuratorClient client;
    private List<String> pathList = new ArrayList<String>();

    public ServiceRegistry(CuratorClient client) {
        this.client = client;
    }
    /**
    * @program: ServiceRegistry
    *
    * @description: register service to zk server
    *
    * @author: LiXiande
    *
    * @create: 23:52 2020/9/26
    **/
    public void registryService(final String host, final int port, final Map<String, Object> serviceMap){
        List<RpcServiceInfo> serviceInfos = new ArrayList<>();
        for(String key : serviceMap.keySet()){
            String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if(serviceInfo.length > 0){
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                logger.info("Register new service: {} ", key);
                if(serviceInfo.length>1){
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                }
                serviceInfos.add(rpcServiceInfo);
            }else {
                logger.warn("Can not get service name and version: {} ", key);
            }
        }
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfos(serviceInfos);
            String serviceData = rpcProtocol.toJson();
            //要注册数据列表
            byte[] data = serviceData.getBytes();
            //zk路径
            String path = Constant.ZK_DATA_PATH +"-"+rpcProtocol.hashCode();
            this.client.createPathData(path,data);
            byte [] bytes =  client.getData(path);
            pathList.add(path);
            logger.info("registe {} new Service, host: {}, port : {}", serviceInfos.size(), host,port);
        } catch (Exception e) {
            logger.error("registe service fail ,exception:\n {} ", e.getMessage());
        }
        client.addConnectionStateListner(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if(connectionState == ConnectionState.CONNECTED){
                    logger.info("connection state : {}, regist service after reconnect", connectionState);
                    registryService(host,port,serviceMap);
                }
            }
        });
    }

    public void unregisterService(){
        logger.info("unregister all service");
        for(String path: pathList){
            try {
                this.client.deletePath(path);
            }catch (Exception e){
                logger.error("delete setvice fail for {}", e.getMessage());
            }
        }
        this.client.close();
    }
}
