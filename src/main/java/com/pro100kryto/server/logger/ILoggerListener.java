package com.pro100kryto.server.logger;

import java.time.LocalDateTime;

public interface ILoggerListener {
    void write(String loggerName, LocalDateTime dateTime, MsgType msgType, String msg);
}
