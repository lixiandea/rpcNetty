package com.lixiande.common.config;

public class Constant {
    public final static int ZK_SESSION_TIMEOUT = 5000;
    public final static int ZK_CONNECTION_TIMEOUT = 5000;

    public final static String ZK_REGISTRY_PATH = "/registry";
    public final static String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
    public final static String ZK_NAMESPACE = "netty-rpc";
}
