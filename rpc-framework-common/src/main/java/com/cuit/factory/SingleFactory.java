package com.cuit.factory;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 17:04
 */

public class SingleFactory {
    private static final Map<String,Object> OBJ_MAP = new ConcurrentHashMap<String,Object>();

    private SingleFactory() {

    }

    public static <T> T getSingle(Class<T> clazz){
        if(clazz == null){
            throw new IllegalArgumentException();
        }
        String clazzS = clazz.toString();
        if(OBJ_MAP.containsKey(clazzS)){
            return clazz.cast(OBJ_MAP.get(clazzS));
        }
        return clazz.cast(OBJ_MAP.computeIfAbsent(clazzS,k->{
            try{
                return clazz.getDeclaredConstructor().newInstance();
            }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                throw new RuntimeException(e.getMessage(),e);
            }
        }));
    }
}
