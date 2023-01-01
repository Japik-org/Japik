package com.japik.networking;

import com.japik.livecycle.ILiveCycle;
import com.japik.settings.Settings;

public interface IProtocol {
    String getName();
    ILiveCycle getLiveCycle();

    IProtocolInstance newInstance(Settings protocolSettings) throws Exception;
}
