package com.lixiande.common.route;

import com.lixiande.client.RpcClientHandler;
import com.lixiande.common.protocol.RpcProtocol;
import com.lixiande.common.protocol.RpcServiceInfo;
import com.lixiande.common.util.ServiceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RpcLoadBalance {
    protected Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectedServerNodes){
        Map<String, List<RpcProtocol>> serviceMap = new HashMap<>();
        if(connectedServerNodes!=null && connectedServerNodes.size()>0){
            for (RpcProtocol protocol: connectedServerNodes.keySet()){
                for(RpcServiceInfo info: protocol.getServiceInfos()){
                    String serviceKey = ServiceUtil.makeServiceKey(info.getServiceName(), info.getVersion());
                    List<RpcProtocol> protocols = serviceMap.get(serviceKey);
                    if(protocols == null){
                        protocols = new ArrayList<>();
                    }
                    protocols.add(protocol);
                    serviceMap.put(serviceKey, protocols);
                }
            }
        }
        return serviceMap;
    }

    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception;
}
