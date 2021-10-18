package com.pro100kryto.server.settings;

public abstract class ByteSettingListener extends TransformedSettingListener<Byte>{
    protected ByteSettingListener() {
        super(Byte::parseByte);
    }
}
