package com.lixiande.common.protocol;

import java.io.Serializable;
import java.util.Objects;
/**
* @program: RpcServiceInfo
*
* @description: service info
 * servicename for deserialize class
 * version for different function
*
* @author: LiXiande
*
* @create: 15:57 2020/9/30
**/
public class RpcServiceInfo implements Serializable {
    private String serviceName;
    private String version;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServiceInfo that = (RpcServiceInfo) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }
}
