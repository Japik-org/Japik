package com.pro100kryto.server.module;

import com.pro100kryto.server.logger.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AModuleConnection<M extends IModule<MC>, MC extends IModuleConnection>
        implements IModuleConnection {

    @Nullable
    protected M module;
    protected final IModuleConnectionCallback callback;
    protected final int id;
    protected final String moduleName;
    protected final String moduleType;
    protected final ILogger logger;

    protected boolean isClosed = false;

    public AModuleConnection(@NotNull M module, ModuleConnectionParams params) {
        this.module = Objects.requireNonNull(module);
        this.callback = Objects.requireNonNull(params.getCallback());
        this.id = params.getId();
        this.moduleName = module.getName();
        this.moduleType = module.getType();
        this.logger = Objects.requireNonNull(params.getLogger());
    }

    @Nullable
    protected final M getModule(){
        return module;
    }

    @Override
    public final int getId() {
        return id;
    }

    @Override
    public final String getModuleName() {
        return moduleName;
    }

    @Override
    public final String getModuleType() {
        return moduleType;
    }

    @Override
    public synchronized final void close(){
        if (isClosed) {
            throw new IllegalStateException();
        }
        isClosed = true;
        onClose();
        module = null;

        callback.onCloseModuleConnection(id);
    }

    @Override
    public final boolean isClosed() {
        return isClosed;
    }

    // virtual

    @Override
    public synchronized boolean isAliveModule() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        return module.getLiveCycle().getStatus().isStarted();
    }

    @Override
    public boolean ping() {
        return true;
    }

    protected void onClose(){}

}
