package com.lixiande.common.serializer.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lixiande.common.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
* @program: HessianSerializer
*
* @description: hessian Serializer.
*
* @author: LiXiande
*
* @create: 16:48 2020/9/30
**/
public class HessianSerializer extends Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output ho = new Hessian2Output(os);
        try {
            ho.writeObject(obj);
            ho.flush();
            byte[] result = os.toByteArray();
            return result;
        }catch (IOException e){
            throw new RuntimeException(e);
        }finally {
            try {
                ho.close();
            }catch (IOException e){
                throw new RuntimeException(e);
            }
            try {
                os.close();
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Hessian2Input hi = new Hessian2Input(is);
        try {
            Object result = hi.readObject();
            return result;
        }catch (IOException e){
            throw new RuntimeException(e);
        }finally {
            try {
                hi.close();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            try {
                is.close();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
