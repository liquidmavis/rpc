package com.cuit.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:51
 */
@Slf4j
public class ChannelProvider {
    private final Map<String,Channel> channelMap;

    public ChannelProvider(){channelMap = new ConcurrentHashMap<String,Channel>(); }

    public Channel get(InetSocketAddress address){
        String add = address.toString();
        if(channelMap.containsKey(add)) {
            Channel channel = channelMap.get(add);
            if(channel != null || channel.isActive()){
                return channel;
            }else{
                channelMap.remove(add);
            }
        }
        return null;
    }

    public void set(InetSocketAddress address,Channel channel){
        channelMap.put(address.toString(), channel);
    }

    public void remove(InetSocketAddress address){
        channelMap.remove(address.toString());
        log.info("Channel map size {}",channelMap.size());
    }
}
