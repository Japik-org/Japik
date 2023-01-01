package com.japik.extension;

import com.japik.logger.ILogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ExtensionConnectionParams {
    private final ILogger logger;
    private final IExtensionConnectionCallback callback;
}
