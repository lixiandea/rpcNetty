package com.lixiande.common.codec;

import lombok.Data;

import java.io.Serializable;
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
