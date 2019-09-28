package com.turan.mq;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * @Description
 * @Date 2019/6/22 11:47
 * @Created by turan
 */
public interface MyOutput
{
    @Output("outputDevice")
    MessageChannel output();
}
