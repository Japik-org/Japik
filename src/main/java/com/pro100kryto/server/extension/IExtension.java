package com.pro100kryto.server.extension;


import com.pro100kryto.server.IStartStopAlive;

public interface IExtension extends IStartStopAlive {
    String getType();

    void sendCommand(String command) throws Throwable;
}
