package com.turan.mq;

import com.alibaba.fastjson.JSON;
import com.turan.api.UniqSequenceApi;
import com.turan.config.GatewayConfig;
import com.turan.example.mq.RegisterResponse;
import com.turan.mq.bo.MQ808Msg;
import com.turan.netty.connection.ChannelManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageChannel;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MQServiceTest
{
    @InjectMocks
    private MQService mqService;
    @Mock
    private MessageChannel outputDevice;
    @Mock
    private ChannelManager channelManager;
    @Mock
    private GatewayConfig gatewayConfig;
    @Mock
    private UniqSequenceApi sequenceApi;

    @Test
    public void inputBaseService() throws InstantiationException, IllegalAccessException
    {
        doReturn(0).when(gatewayConfig).getProtocolVersion();
        doReturn(1).when(sequenceApi).nextId();

        RegisterResponse response = new RegisterResponse();
        response.setDevMsgUid(1);
        response.setResult(3);
        response.setAuthCode(null);

        MQ808Msg mq808Msg = new MQ808Msg();
        mq808Msg.setChannelId(1);
        mq808Msg.setMsgType("REGISTER_RESPONSE");
        mq808Msg.setMobile("13452202456");
        mq808Msg.setMsgBody(JSON.toJSONString(response));

        mqService.inputBaseService(mq808Msg);

        verify(channelManager,times(1)).write(anyInt(),notNull());
    }
}