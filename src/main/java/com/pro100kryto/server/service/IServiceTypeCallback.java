package com.pro100kryto.server.service;

import com.pro100kryto.server.logger.ILogger;

public interface IServiceTypeCallback {
    ILogger getLogger();
    String getSetting(String key);
    String getSettingOrDefault(String key, String defaultVal);
}
