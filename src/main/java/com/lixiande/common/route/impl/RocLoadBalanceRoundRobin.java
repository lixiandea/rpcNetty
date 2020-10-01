package com.lixiande.common.route.impl;

import com.lixiande.client.RpcClientHandler;
import com.lixiande.common.protocol.RpcProtocol;
import com.lixiande.common.route.RpcLoadBalance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
/**
* @program: RocLoadBalanceRoundRobin
*
* @description: RoundRobin load balance strategy
*
* @author: LiXiande
*
* @create: 16:44 2020/9/30
**/
public class RocLoadBalanceRoundRobin extends RpcLoadBalance {
    // count for round robin
    private AtomicInteger roundRobin = new AtomicInteger(0);
    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        //get service and address list
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
    // round robin
    public RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        // Round robin
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return addressList.get(index);
    }
}
