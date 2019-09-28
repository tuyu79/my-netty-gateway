package com.turan.mq.bo;

import lombok.Data;

@Data
public class MQ808Msg
{
    private Integer channelId;
    private String msgType;
    private Integer msgId;
    private String mobile;
    private Integer msgUid;
    private String msgBody;
}
