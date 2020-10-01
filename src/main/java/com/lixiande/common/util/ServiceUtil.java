package com.lixiande.common.util;
/**
* @program: ServiceUtil
*
* @description: generate service key from service name#version
*
* @author: LiXiande
*
* @create: 17:03 2020/9/30
**/
public class ServiceUtil {
    public static final String SERVICE_CONCAT_TOKEN = "#";
    public static String makeServiceKey(String intefaceName, String version){
        String serviceKey = intefaceName;
        if(version!=null && version.trim().length() > 0){
            serviceKey += SERVICE_CONCAT_TOKEN.concat(version);
        }
        return serviceKey;
    }
}
