package com.turan.mq;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.turan.api.UniqSequenceApi;
import com.turan.config.GatewayConfig;
import com.turan.enums.DMessageEnum;
import com.turan.example.protocol.imessage.IT808Message;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.MessageUtil;
import com.turan.mq.bo.MQ808Msg;
import com.turan.netty.connection.ChannelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MQService
{
    @Autowired
    private MessageChannel outputDevice;
    @Autowired
    private ChannelManager channelManager;
    @Autowired
    private GatewayConfig gatewayConfig;
    @Autowired
    private UniqSequenceApi sequenceApi;

    public void gatewayUp(MQ808Msg mq808Msg)
    {
        Message<MQ808Msg> message = MessageBuilder.createMessage(mq808Msg, new MessageHeaders(ImmutableMap.of()));
        outputDevice.send(message);
        log.info("send mq msg,msg : {}", mq808Msg);
    }

    @StreamListener("inputBaseService")
    public void inputBaseService(MQ808Msg mq808Msg) throws IllegalAccessException, InstantiationException
    {
        DMessageEnum msgEnum = DMessageEnum.getEnumByType(mq808Msg.getMsgType(), gatewayConfig.getProtocolVersion());
        if (msgEnum == null)
        {
            log.warn("msg type not exist,mobile: [{}],msgType: [{}],protocolVersion: [{}]"
                    , mq808Msg.getMobile(), mq808Msg.getMsgType(), gatewayConfig.getProtocolVersion());
            return;
        }

        IT808Message message = (IT808Message) JSON.parseObject(mq808Msg.getMsgBody(), msgEnum.getClzz());

        Header header = new Header();
        header.setMsgId(msgEnum.getMsgId());
        header.setBodyAttr(MessageUtil.bodyAttr(0, 0, 0, message.array().length));
        header.setMobile(mq808Msg.getMobile());
        header.setMsgUid(sequenceApi.nextId());
        header.setPkgDivideInfo(null);

        T808Message t808Message = new T808Message();
        t808Message.setHeader(header);
        t808Message.setBody(message.array());

        channelManager.write(mq808Msg.getChannelId(), t808Message);

        log.info("get msg from base service: {}", t808Message);
    }
}
