package com.japik.settings;

public abstract class ByteSettingListener extends TransformedSettingListener<Byte>{
    protected ByteSettingListener() {
        super(Byte::parseByte);
    }
}
