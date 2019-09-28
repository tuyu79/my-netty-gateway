package com.turan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("gateway")
@Data
public class GatewayConfig
{
    private int protocolVersion;
    private int nettyPort;
}
