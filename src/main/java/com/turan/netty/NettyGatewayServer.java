package com.turan.netty;

import com.turan.mq.MQService;
import com.turan.netty.codec.T808Decoder;
import com.turan.netty.codec.T808Encoder;
import com.turan.netty.connection.ChannelManager;
import com.turan.netty.connection.ConnectionHandler;
import com.turan.netty.message.InboundsMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Date 2019/5/22 21:50
 * @Created by turan
 */
@Slf4j
public class NettyGatewayServer
{
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroupSelector; // 处理获取到的网络包
    private final EventLoopGroup eventLoopGroupBoss; // 监听连接请求，创建SocketChannel,并注册到selector
    private DefaultEventExecutorGroup defaultEventExecutorGroup; // 执行处理编解码，连接管理的handler

    private final static int EVENT_LOOP_BOSS_THREADS = 1;
    private final static int EVENT_LOOP_SELECTOR_THREADS = 3;
    private final static int SERVER_WORKER_THREADS = 8;

    private final MQService mqService;
    private final ChannelManager channelManager;
    private final int protocolVersion;
    private final int port;

    public NettyGatewayServer(MQService mqService,
                              ChannelManager channelManager,
                              int protocolVersion,
                              int port)
    {
        this.mqService = mqService;
        this.channelManager = channelManager;
        this.protocolVersion = protocolVersion;
        this.port = port;

        this.serverBootstrap = new ServerBootstrap();

        if (useEpoll())
        {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(EVENT_LOOP_BOSS_THREADS, new ThreadFactory()
            {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, String.format("NettyEPOLLBoss_%d", this.threadIndex.incrementAndGet()));
                }
            });

            this.eventLoopGroupSelector = new EpollEventLoopGroup(EVENT_LOOP_SELECTOR_THREADS, new ThreadFactory()
            {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = EVENT_LOOP_SELECTOR_THREADS;

                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        } else
        {
            this.eventLoopGroupBoss = new NioEventLoopGroup(EVENT_LOOP_BOSS_THREADS, new ThreadFactory()
            {
                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, String.format("NettyNIOBoss_%d", this.threadIndex.incrementAndGet()));
                }
            });

            this.eventLoopGroupSelector = new NioEventLoopGroup(EVENT_LOOP_SELECTOR_THREADS, new ThreadFactory()
            {
                private AtomicInteger threadIndex = new AtomicInteger(0);
                private int threadTotal = EVENT_LOOP_SELECTOR_THREADS;

                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
                }
            });
        }

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                SERVER_WORKER_THREADS,
                new ThreadFactory()
                {

                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r)
                    {
                        return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
                    }
                });

    }

    private boolean useEpoll()
    {
        return false;
    }

    public void start()
    {
        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception
                    {
                        ch.pipeline()
                                .addLast(defaultEventExecutorGroup,
                                        new T808Encoder(),
                                        new T808Decoder(),
                                        new IdleStateHandler(0, 0, 120),
                                        new ConnectionHandler(channelManager),
                                        new InboundsMessageHandler(mqService, protocolVersion)
                                );
                    }
                });

        try
        {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            sync.channel().localAddress();
            log.info("netty server start at 9090");
        } catch (InterruptedException e1)
        {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }
    }
}
