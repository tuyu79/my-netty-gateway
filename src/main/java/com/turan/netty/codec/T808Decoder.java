package com.turan.netty.codec;

import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.BufferUtil;
import com.turan.example.protocol.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

// todo turan 分包逻辑的实现

@Slf4j
public class T808Decoder extends ByteToMessageDecoder
{

    private static final int MSG_BODY_LENGTH_BITS = 0x1FF;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
    {
        try
        {
            // 不是以0x7E开头的消息,直接舍弃当前字节
            if (in.getUnsignedByte(in.readerIndex()) != T808Message.MSG_MARK)
            {
                in.readUnsignedByte();
                return;
            }

            int endMarkIndex = in.indexOf(in.readerIndex() + 1, in.readerIndex() + in.readableBytes(), (byte) T808Message.MSG_MARK);

            if (endMarkIndex == -1) // 没有0X7E结束符,消息还没传完
            {
                return;
            }

            ByteBuf msgBuf = in.readBytes(endMarkIndex - in.readerIndex() + 1);


            Object result = decodeT808Msg(msgBuf);
            if (result != null)
            {
                out.add(result);
            }

        } catch (Exception e)
        {
            log.error("decode msg error!", e);
        }
    }

    private Object decodeT808Msg(ByteBuf msgBuf)
    {
        byte[] bytes = MessageUtil.transfer7D27E(BufferUtil.readBytes(msgBuf));
        T808Message message = new T808Message();
        message.fill(bytes);

        int bodyAttr = message.getHeader().getBodyAttr();
        int bodyLength = bodyLength(bodyAttr);
        boolean ifDivide = MessageUtil.ifPkgDivide(bodyAttr);

        if (bodyLength != message.getBody().length)
        {
            log.warn("message bodyLen error,fromBodyAttr: [{}],bodyMsgLen: [{}]", bodyLength, message.getBody().length);
            return null;
        }

        int validCode = MessageUtil.validCode(message.validArray());
        if (message.getValid() != validCode)
        {
            log.warn("valid code error!");
            return null;
        }


        log.info("decode 808 msg success,mobile: [{}],msgId: [{}],msgUid: [{}]",
                message.getHeader().getMobile(),message.getHeader().getHexMsgId(),
                message.getHeader().getMsgUid());

        return message;
    }

    private int bodyLength(int bodyAttr)
    {
        return bodyAttr & MSG_BODY_LENGTH_BITS;
    }

    public static void main(String[] args)
    {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{0x01, 0x02, 0x03, 0x01});

        int i = -1;

        System.out.println(byteBuf.readerIndex());
        System.out.println(byteBuf.readerIndex() + byteBuf.readableBytes());
        System.out.println(i = byteBuf.indexOf(byteBuf.readerIndex() + 1, byteBuf.readerIndex() + byteBuf.readableBytes(), (byte) 0x01));

        ByteBuf byteBuf1 = byteBuf.readBytes(i - byteBuf.readerIndex() + 1);
        System.out.println(byteBuf1.toString());
    }
}
