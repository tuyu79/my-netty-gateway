package com.turan.netty.codec;

import com.turan.example.protocol.message.platform.P_8001;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class T808EncoderTest
{
    private T808Message message;

    @Before
    public void init() throws Exception
    {
        message = new T808Message();

        Header header = new Header();
        header.setMsgId(P_8001.MSG_ID);
        header.setMobile("13452202456");
        header.setMsgUid(1);
        header.setPkgDivideInfo(null);

        P_8001 p_8001 = new P_8001();
        p_8001.setDevMsgUid(1);
        p_8001.setDevMsgId(1);
        p_8001.setResult(0);

        header.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, p_8001.array().length));

        message.setHeader(header);
        message.setBody(p_8001.array());
    }

    @Test
    public void whenOutboundsMsgThenReadMsg()
    {
        EmbeddedChannel channel = new EmbeddedChannel(new T808Encoder());

        channel.writeOutbound(message);
        ByteBuf byteBuf = (ByteBuf) channel.readOutbound();
        byte[] readOutboundMsg = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(readOutboundMsg);
        assertArrayEquals(message.array(), MessageUtil.transfer7D27E(readOutboundMsg));
    }
}