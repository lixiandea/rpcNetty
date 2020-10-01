package com.lixiande.common.codec;

import lombok.Data;

import java.io.Serializable;
/**
* @program: RpcResponse
*
* @description: response class
 * include error、 result、requestId
*
* @author: LiXiande
*
* @create: 15:25 2020/9/30
**/
@Data
public class RpcResponse implements Serializable {
    public static final long serialVersionUID=8215493329459772524L;
    private String error;
    private Object result;
    private String requestId;

    public boolean isError(){
        return error!=null;
    }
}
