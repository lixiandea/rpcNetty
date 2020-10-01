package com.lixiande.common.codec;

/**
* @program: Beat
*
* @description: heart beat for discovery to zookeeper
*
* @author: LiXiande
*
* @create: 14:55 2020/9/30
**/
public class Beat {
    public static final int BEAT_INTERVAL = 30;
    public static final int BEAT_TIMEOUT= 3* BEAT_INTERVAL;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static{
        BEAT_PING = new RpcRequest(){};
        BEAT_PING.setRequestId(BEAT_ID);
    }
}
