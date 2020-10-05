package com.lixiande.common.codec;

import lombok.Data;

import java.io.Serializable;

/**
* @program: RpcRequest
*
* @description: request class
 * include id, classname, methodName, parameterTypes, parameters,
 * and version which include invoke function needed
*
* @author: LiXiande
*
* @create: 15:24 2020/9/30 
**/
@Data
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -2524587347775862771L;
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private String version;
}
