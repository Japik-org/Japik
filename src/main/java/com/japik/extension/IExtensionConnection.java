package com.japik.extension;

import com.japik.livecycle.LiveCycleStatus;

import java.io.Closeable;

public interface IExtensionConnection extends Closeable {
    String getExtensionType();
    LiveCycleStatus getExtensionStatus();
    void close();
    boolean isClosed();
}
