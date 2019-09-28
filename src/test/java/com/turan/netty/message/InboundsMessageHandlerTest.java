package com.turan.netty.message;

import com.turan.example.protocol.message.dev.D_0100;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import com.turan.mq.MQService;
import com.turan.mq.bo.MQ808Msg;
import com.turan.netty.codec.T808Decoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InboundsMessageHandlerTest
{
    private T808Message message;
    @Mock
    private MQService mqService;

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
    public void whenInboundsMsgComingThenReadMsg()
    {
        EmbeddedChannel channel = new EmbeddedChannel(
                new T808Decoder(),
                new InboundsMessageHandler(mqService,0)
        );

        channel.writeInbound(Unpooled.wrappedBuffer(message.array()));
        verify(mqService,times(1)).gatewayUp(any(MQ808Msg.class));
    }
}