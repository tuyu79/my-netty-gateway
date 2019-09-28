package com.turan.config;

import com.turan.mq.MQService;
import com.turan.netty.NettyGatewayServer;
import com.turan.netty.connection.ChannelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class NettyConfig
{
    @Autowired
    private MQService mqService;
    @Autowired
    private GatewayConfig gatewayConfig;
    @Autowired
    private ChannelManager channelManager;

    @PostConstruct
    public void start()
    {
        NettyGatewayServer gatewayServer = new NettyGatewayServer(mqService,
                channelManager,
                gatewayConfig.getProtocolVersion(),
                gatewayConfig.getNettyPort());
        gatewayServer.start();
    }
}
