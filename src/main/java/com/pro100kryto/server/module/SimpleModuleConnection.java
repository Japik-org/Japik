package com.pro100kryto.server.module;

public final class SimpleModuleConnection <M extends IModule<ISimpleModuleConnection>>
        extends AModuleConnection<M, ISimpleModuleConnection>{

    public SimpleModuleConnection(M module, ModuleConnectionParams params) {
        super(module, params);
    }
}
