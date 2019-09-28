package com.turan.netty.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Slf4j
public class ConnectionHandler extends ChannelDuplexHandler
{
    private ChannelManager channelManager;

    public ConnectionHandler(ChannelManager channelManager)
    {
        this.channelManager = channelManager;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception
    {
        log.info("NETTY SERVER PIPELINE: channelRegistered,ip: [{}]", getRemoteAddress(ctx.channel()));
        super.channelRegistered(ctx);
        channelManager.add(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
    {
        log.info("NETTY SERVER PIPELINE: channelUnRegistered,ip: [{}]", getRemoteAddress(ctx.channel()));
        super.channelUnregistered(ctx);
        channelManager.close(ctx.channel().hashCode());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        log.info("NETTY SERVER PIPELINE: userEventTriggered,ip: [{}]", getRemoteAddress(ctx.channel()));
        ctx.fireUserEventTriggered(evt);
        if (evt instanceof IdleStateEvent)
        {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE)
            {
                channelManager.close(ctx.channel().hashCode());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        log.info("NETTY SERVER PIPELINE: exceptionCaught,ip: [{}]", getRemoteAddress(ctx.channel()));
        channelManager.close(ctx.channel().hashCode());
    }

    private String getRemoteAddress(Channel channel)
    {
        SocketAddress socketAddress = channel.remoteAddress();
        if (socketAddress instanceof InetSocketAddress)
        {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
            return inetSocketAddress.getAddress().getHostAddress();
        }
        return "";
    }
}
