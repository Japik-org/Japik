package com.japik.networking;

import com.japik.Japik;
import lombok.Getter;

@Getter
public final class Networking {
    private final ProtocolCollection protocolCollection;
    private final RemoteCollection remoteCollection;

    public Networking(Japik server) {
        protocolCollection = new ProtocolCollection();
        remoteCollection = new RemoteCollection(server);
    }

}
