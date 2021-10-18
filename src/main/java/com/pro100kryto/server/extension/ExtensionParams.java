package com.pro100kryto.server.extension;

import com.pro100kryto.server.Server;
import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.logger.ILogger;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ExtensionParams {
    private final Server server;
    private final String extensionType;
    private final ILogger logger;
    private final Tenant extensionAsTenant;
}
