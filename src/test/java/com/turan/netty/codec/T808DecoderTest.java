package com.turan.netty.codec;

import com.turan.example.protocol.message.dev.D_0100;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class T808DecoderTest
{

    private T808Message message;

    @Before
    public void init() throws Exception
    {
        message = new T808Message();

        Header header = new Header();
        header.setMsgId(D_0100.MSG_ID);
        header.setMobile("13452202456");
        header.setMsgUid(1);
        header.setPkgDivideInfo(null);

        D_0100 d_0100 = new D_0100();
        d_0100.setProvinceId(1);
        d_0100.setCityId(1);
        d_0100.setManufacturer("MD5");
        d_0100.setTerminalType("D5X");
        d_0100.setTerminalId("123456");
        d_0100.setPlateColor(2);
        d_0100.setVehicleMark("测A0002");

        header.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, d_0100.array().length));

        message.setHeader(header);
        message.setBody(d_0100.array());
    }

    @Test
    public void whenInboundByteMsgThenRead808ObjectMsg()
    {
        EmbeddedChannel channel = new EmbeddedChannel(new T808Decoder());

        channel.writeInbound(Unpooled.wrappedBuffer(message.array()));

        T808Message readInboundMsg = channel.readInbound();
        assertEquals(readInboundMsg, message);
    }

    @Test
    public void whenInboundsLengthLessThan5ThenReadNull()
    {
        EmbeddedChannel channel = new EmbeddedChannel(new T808Decoder());

        channel.writeInbound(Unpooled.wrappedBuffer(Arrays.copyOf(message.array(), 3)));

        T808Message readInboundMsg = (T808Message) channel.readInbound();
        assertNull(readInboundMsg);
    }

    @Test
    public void whenInboundsMsgNotStartWith0x7EThenReadNull()
    {
        EmbeddedChannel channel = new EmbeddedChannel(new T808Decoder());

        byte[] bytes = new byte[]{0x10, 0x11, 0x22};

        channel.writeInbound(Unpooled.wrappedBuffer(bytes));

        T808Message readInboundMsg = (T808Message) channel.readInbound();
        assertNull(readInboundMsg);
    }

    @Test
    public void whenMsgNotInOnePacketThenReadMsg()
    {
        byte[] array = message.array();

        EmbeddedChannel channel = new EmbeddedChannel(new T808Decoder());

        channel.writeInbound(Unpooled.wrappedBuffer(Arrays.copyOfRange(array, 0, 10)));
        channel.writeInbound(Unpooled.wrappedBuffer(Arrays.copyOfRange(array, 10, 20)));
        channel.writeInbound(Unpooled.wrappedBuffer(Arrays.copyOfRange(array, 20, array.length)));

        T808Message readInboundMsg = (T808Message) channel.readInbound();
        assertEquals(readInboundMsg, message);
    }

    @Test
    public void whenMsgNotEndWith0x7EThenReadNull()
    {
        byte[] array = message.array();
        array[array.length - 1] = 0x17;

        EmbeddedChannel channel = new EmbeddedChannel(new T808Decoder());

        channel.writeInbound(Unpooled.wrappedBuffer(array));

        T808Message readInboundMsg = (T808Message) channel.readInbound();
        assertNull(readInboundMsg);
    }

    @Test
    public void whenMsgValidCodeErrorThenReadNull()
    {
        byte[] array = message.array();
        array[array.length - 2] = (byte) (array[array.length - 2] + 1);
        EmbeddedChannel channel = new EmbeddedChannel(new T808Decoder());

        channel.writeInbound(Unpooled.wrappedBuffer(array));

        T808Message readInboundMsg = (T808Message) channel.readInbound();
        assertNull(readInboundMsg);
    }
}
