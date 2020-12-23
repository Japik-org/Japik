package com.pro100kryto.server;

import com.pro100kryto.server.extension.ExtensionLoader;
import com.pro100kryto.server.extension.IExtension;
import com.pro100kryto.server.properties.ProjectProperties;
import com.pro100kryto.server.service.manager.IServiceManagerControl;


public interface IServerControl extends IServer{
    void start() throws Throwable;
    void stop(boolean force) throws Throwable;

    IServiceManagerControl getServiceManager();

    ExtensionLoader getExtensionCreator();
    void addExtension(IExtension extension);
    void removeExtension(String type);

    ProjectProperties getProjectProperties();
}
