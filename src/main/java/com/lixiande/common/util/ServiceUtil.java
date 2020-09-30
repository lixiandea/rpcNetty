package com.lixiande.common.util;

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
