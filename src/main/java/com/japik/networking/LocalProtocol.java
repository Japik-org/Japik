package com.japik.networking;

import com.japik.Japik;
import com.japik.service.*;
import com.japik.settings.Settings;

import java.rmi.RemoteException;

public final class LocalProtocol extends AProtocol {
    public static final String name = "local";

    public LocalProtocol(Japik server, Settings settings) {
        super(name, server, settings);
    }

    @Override
    protected IProtocolInstance newInstanceImpl(Settings protocolSettings) {
        return new LocalProtocolInstance();
    }

    private final class LocalProtocolInstance extends AProtocolInstance {

        public LocalProtocolInstance() {
            super(LocalProtocol.name);
        }

        @Override
        protected boolean existsServiceImpl(String serviceName) {
            return server.getServiceLoader().exists(serviceName);
        }

        @Override
        protected <SC extends IServiceConnection> SC getServiceConnectionImpl(String serviceName) throws RemoteException, ServiceNotFoundException {
            final IService<SC> service = server.getServiceLoader().getServiceOrThrow(serviceName);
            return service.getServiceConnection();
        }

        @Override
        protected <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafeImpl(String serviceName) {
            return new ServiceConnectionSafeFromLoader<>(
                    server.getServiceLoader(),
                    serviceName
            );
        }

        @Override
        protected void onClose() {
        }
    }
}
