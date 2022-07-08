package com.japik.utils;

public interface RunnableThrow {
    /**
     * The general contract of the method run is that it may take any action whatsoever.
     * @throws Throwable
     */
    void run() throws Throwable;
}
