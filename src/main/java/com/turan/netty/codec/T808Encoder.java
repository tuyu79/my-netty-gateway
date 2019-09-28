package com.turan.netty.codec;

import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

// todo turan 分包逻辑的实现

public class T808Encoder extends MessageToByteEncoder<T808Message>
{
    @Override
    protected void encode(ChannelHandlerContext ctx, T808Message msg, ByteBuf out) throws Exception
    {
        out.writeBytes(MessageUtil.transfer7E27D(msg.array()));
    }
}
