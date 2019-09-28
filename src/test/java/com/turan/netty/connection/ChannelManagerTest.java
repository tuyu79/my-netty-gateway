package com.turan.netty.connection;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChannelManagerTest
{

    @Mock
    private ChannelManager channelManager;

    @Test
    public void whenConnectThenChannelTableSizeIs1() throws IOException
    {

        EmbeddedChannel channel = new EmbeddedChannel(
                new ConnectionHandler(channelManager)
        );

        channel.connect(new InetSocketAddress("localhost", 9999));
        verify(channelManager,times(1)).add(any(Channel.class));

        channel.disconnect();
        verify(channelManager,times(1)).close(any(Integer.class));
    }
}
