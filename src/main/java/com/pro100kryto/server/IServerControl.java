package com.pro100kryto.server;

import com.pro100kryto.server.extension.ExtensionLoader;
import com.pro100kryto.server.extension.IExtension;
import com.pro100kryto.server.properties.ProjectProperties;
import com.pro100kryto.server.service.manager.IServiceManagerControl;

import java.net.URL;


public interface IServerControl extends IServer{
    void start() throws Throwable;
    void stop(boolean force) throws Throwable;

    IServiceManagerControl getServiceManager();

    ExtensionLoader getExtensionCreator();
    void addExtension(IExtension extension);
    void removeExtension(String type);

    void setSetting(String key, String val);

    void addBaseLib(URL url);

    ProjectProperties getProjectProperties();
}
