package com.lixiande.common.protocol;

import com.lixiande.common.util.JsonUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class RpcProtocol implements Serializable {
    public static final long serialVersionUID = -1102180003395190700L;
    private String host;
    private int port;
    private List<RpcServiceInfo> serviceInfos;

    public String toJson(){
        String json = JsonUtil.objectToJson(this);
        RpcProtocol obj = JsonUtil.jsonToObject (json, RpcProtocol.class);
        return json;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfos() {
        return serviceInfos;
    }

    public void setServiceInfos(List<RpcServiceInfo> serviceInfos) {
        this.serviceInfos = serviceInfos;
    }

    public static RpcProtocol fromJson(String json){
        return JsonUtil.jsonToObject(json, RpcProtocol.class);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }else {
            if(obj == null || getClass()!=obj.getClass()) return false;
            RpcProtocol that = (RpcProtocol) obj;
            return port == this.port &&
                    Objects.equals(host,that.host) &&
                    isListEquals(that.serviceInfos, this.getServiceInfos());
        }
    }
    private boolean isListEquals(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
        if (thisList == null && thatList == null) {
            return true;
        }
        if ((thisList == null && thatList != null)
                || (thisList != null && thatList == null)
                || (thisList.size() != thatList.size())) {
            return false;
        }
        return thisList.containsAll(thatList) && thatList.containsAll(thisList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host,port,serviceInfos);
    }
}
