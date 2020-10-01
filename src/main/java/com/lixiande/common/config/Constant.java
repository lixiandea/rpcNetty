package com.lixiande.common.config;
/**
* @program: Constant
*
* @description:  constant config for zookeeper or other environment
*
* @author: LiXiande
*
* @create: 15:27 2020/9/30
**/
public class Constant {
    // session timeout
    public final static int ZK_SESSION_TIMEOUT = 5000;
    // connect timeout
    public final static int ZK_CONNECTION_TIMEOUT = 5000;
    //registry path
    public final static String ZK_REGISTRY_PATH = "/registry";
    // data path
    public final static String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
    //namespace
    public final static String ZK_NAMESPACE = "netty-rpc";
}
