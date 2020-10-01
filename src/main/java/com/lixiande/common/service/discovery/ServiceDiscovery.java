package com.lixiande.common.service.discovery;

import com.lixiande.client.ConnectionManager;
import com.lixiande.common.config.Constant;
import com.lixiande.common.protocol.RpcProtocol;
import com.lixiande.common.zookeeper.CuratorClient;
import com.sun.crypto.provider.PBEWithMD5AndDESCipher;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
/**
* @program: ServiceDiscovery
*
* @description: service discovery for rpc client to get available service from registry
*
* @author: LiXiande
*
* @create: 16:49 2020/9/30
**/
public class ServiceDiscovery {
    public static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CuratorClient client;

    public ServiceDiscovery(String registryAddress){
        this.client = new CuratorClient(registryAddress);
        discoveryService();
    }

    private void discoveryService(){
        try{
            logger.info("Get initial service info");
            getServiceAndUpdateServer();
            client.watchPathChildrenNode(Constant.ZK_REGISTRY_PATH, (curatorFramework, pathChildrenCacheEvent) -> {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                switch (type){
                    case CONNECTION_RECONNECTED:
                        logger.info("Reconncted to zk, try to get lasest service list");
                        getServiceAndUpdateServer();;
                        break;
                    case CHILD_ADDED:
                    case CHILD_UPDATED:
                    case CHILD_REMOVED:
                        logger.info("Service info changed, try to ger latest service list");
                        getServiceAndUpdateServer();
                        break;
                }
            });

        }catch (Exception e){
            logger.error("Watch node exception : " + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(){
        try {
            List<String> nodeList = client.getChildren(Constant.ZK_REGISTRY_PATH);
            List<RpcProtocol> protocols = new ArrayList<>();
            for(String node: nodeList){
                logger.debug("Server node : " + node);
                byte[] bytes = client.getData(Constant.ZK_REGISTRY_PATH+"/"+ node);
                String json = new String(bytes);
                RpcProtocol protocol = RpcProtocol.fromJson(json);
                protocols.add(protocol);
            }
            updateConnectedServer(protocols);
        } catch (Exception e) {
            logger.error("Get node Exception : " + e.getMessage());
        }
    }

    private void updateConnectedServer(List<RpcProtocol> protocols){
        ConnectionManager.getInstance().updateConnectServer(protocols);
    }

    public void stop(){
        client.close();
    }


}
