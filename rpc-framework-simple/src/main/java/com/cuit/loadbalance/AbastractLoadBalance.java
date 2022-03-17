package com.cuit.loadbalance;

import com.cuit.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:45
 */

public abstract class AbastractLoadBalance implements Loadbalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddressList, RpcRequest request) {
        int len = serviceAddressList.size();
        if(len == 0){
            return null;
        }
        if(len == 1){
            return serviceAddressList.get(0);
        }
        return doSelect(serviceAddressList,request);
    }

    protected abstract String doSelect(List<String> serviceAddressList, RpcRequest request);
}
