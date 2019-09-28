package com.turan.netty.connection;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

@Component
public class ChannelManager
{
    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void add(Channel channel)
    {
        allChannels.add(channel);
    }

    public void close(Integer channelHash)
    {
        allChannels.close(new ChannelMatcher()
        {
            @Override
            public boolean matches(Channel channel)
            {
                return channelHash == channel.hashCode();
            }
        });
    }
    public void write(Integer channelHash,Object msg)
    {
        allChannels.writeAndFlush(msg,new ChannelMatcher()
        {
            @Override
            public boolean matches(Channel channel)
            {
                return channelHash == channelHash.hashCode();
            }
        });
    }
}
