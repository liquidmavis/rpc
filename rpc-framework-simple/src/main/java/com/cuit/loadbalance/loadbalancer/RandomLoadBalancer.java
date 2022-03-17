package com.cuit.loadbalance.loadbalancer;

import com.cuit.loadbalance.AbastractLoadBalance;
import com.cuit.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:47
 */

public class RandomLoadBalancer extends AbastractLoadBalance{
    @Override
    protected String doSelect(List<String> serviceAddressList, RpcRequest request) {
        Random random = new Random();
        return serviceAddressList.get(random.nextInt(serviceAddressList.size()));
    }
}
