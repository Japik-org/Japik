package com.japik.logger;

import java.time.LocalDateTime;

public interface ILoggerListener {
    void write(String loggerName, LocalDateTime dateTime, MsgType msgType, String msg);
}
