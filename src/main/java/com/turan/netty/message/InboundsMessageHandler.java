package com.turan.netty.message;

import com.alibaba.fastjson.JSON;
import com.turan.enums.DMessageEnum;
import com.turan.example.protocol.imessage.IT808Message;
import com.turan.example.protocol.structure.Header;
import com.turan.example.protocol.structure.T808Message;
import com.turan.mq.MQService;
import com.turan.mq.bo.MQ808Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InboundsMessageHandler extends SimpleChannelInboundHandler<T808Message>
{

    private final MQService mqService;
    private int protocolVersion;

    public InboundsMessageHandler(MQService mqService, int protocolVersion)
    {
        super();
        this.mqService = mqService;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T808Message msg) throws Exception
    {
        try
        {
            Header header = msg.getHeader();
            DMessageEnum messageEnum = DMessageEnum.getEnumByMsgId(header.getMsgId(), protocolVersion);
            if (messageEnum == null)
            {
                log.warn("msg id not support,mobile: [{}],msgId: [{}],protocolVersion: [{}]",
                        header.getMobile(), header.getHexMsgId(), protocolVersion);
                return;
            }

            MQ808Msg mq808Msg = new MQ808Msg();
            mq808Msg.setChannelId(ctx.channel().hashCode());
            mq808Msg.setMsgType(messageEnum.getMsgType());
            mq808Msg.setMsgId(messageEnum.getMsgId());
            mq808Msg.setMobile(header.getMobile());
            mq808Msg.setMsgUid(header.getMsgUid());

            IT808Message body = (IT808Message) messageEnum.getClzz().newInstance();
            if (msg.getBody() != null && msg.getBody().length > 0)
            {
                body.fill(msg.getBody());
            }

            mq808Msg.setMsgBody(JSON.toJSONString(body));

            mqService.gatewayUp(mq808Msg);

        } catch (Exception e)
        {
            log.error("handle msg error,mobile: [{}],msgId: [{}]",
                    msg.getHeader().getMobile(), msg.getHeader().getHexMsgId(), e);
        }
    }
}
