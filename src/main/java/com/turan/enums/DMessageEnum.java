package com.turan.enums;

import com.turan.example.protocol.message.dev.D_0003;
import com.turan.example.protocol.message.dev.D_0100;
import com.turan.example.protocol.message.dev.D_0102;
import com.turan.example.protocol.message.dev.D_0200;
import com.turan.example.protocol.message.platform.P_8001;
import com.turan.example.protocol.message.platform.P_8100;
import com.turan.example.protocol.message.platform.P_9208;
import lombok.Getter;

/**
 * @Description 808消息id对应的实体对象
 * @Date 2019/5/26 16:11
 * @Created by turan
 */
@Getter
public enum DMessageEnum
{
    _0100(D_0100.MSG_ID, ProtocolVersion.STANDARD.code, D_0100.class, "REGISTER"),
    _0003(D_0003.MSG_ID, ProtocolVersion.STANDARD.code, D_0003.class, "UNREGISTER"),
    _0102(D_0102.MSG_ID, ProtocolVersion.STANDARD.code, D_0102.class, "AUTH"),
    _0200(D_0200.MSG_ID, ProtocolVersion.STANDARD.code, D_0200.class, "GPS"),
    _8100(P_8100.MSG_ID, ProtocolVersion.STANDARD.code, P_8100.class, "REGISTER_RESPONSE"),
    _8001(P_8001.MSG_ID, ProtocolVersion.STANDARD.code, P_8001.class, "COMMON_RESPONSE"),
    _9208(P_9208.MSG_ID, ProtocolVersion.STANDARD.code, P_9208.class, "UPLOAD_ATTACHMENT"),
    ;

    private int msgId;
    private int protocolVersion;
    private Class clzz;
    private String msgType;

    DMessageEnum(int msgId, int protocolVersion, Class clzz, String msgType)
    {
        this.msgId = msgId;
        this.protocolVersion = protocolVersion;
        this.clzz = clzz;
        this.msgType = msgType;
    }

    public static DMessageEnum getEnumByMsgId(int msgId, int protocolVersion)
    {
        for (DMessageEnum value : DMessageEnum.values())
        {
            if (value.getMsgId() == msgId && value.getProtocolVersion() == protocolVersion)
                return value;
        }
        return null;
    }

    public static DMessageEnum getEnumByType(String tag, int protocolVersion)
    {
        for (DMessageEnum value : DMessageEnum.values())
        {
            if (value.getMsgType().equals(tag) && value.getProtocolVersion() == protocolVersion)
                return value;
        }
        return null;
    }

    private enum ProtocolVersion
    {
        STANDARD(0, "标准808"),
        SHAN_BIAO(1, "陕标"),
        ;

        private int code;
        private String desc;

        ProtocolVersion(int code, String desc)
        {
            this.code = code;
            this.desc = desc;
        }
    }
}
