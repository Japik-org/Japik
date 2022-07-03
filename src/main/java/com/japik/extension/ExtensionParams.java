package com.japik.extension;

import com.japik.Japik;
import com.japik.dep.Tenant;
import com.japik.logger.ILogger;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ExtensionParams {
    private final Japik server;
    private final String extensionType;
    private final String extensionName;
    private final Tenant extensionTenant;
    private final ILogger logger;
}
