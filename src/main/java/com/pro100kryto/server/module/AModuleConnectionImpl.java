package com.pro100kryto.server.module;

import com.pro100kryto.server.logger.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AModuleConnectionImpl<M extends IModule<MC>, MC extends IModuleConnection> implements IModuleConnection {
    @Nullable
    private M module;
    protected final ILogger logger;
    protected final String moduleType;
    protected final String moduleName;
    protected boolean isClosed = false;


    public AModuleConnectionImpl(@NotNull M module, ILogger logger) {
        this.module = Objects.requireNonNull(module);
        this.logger = Objects.requireNonNull(logger);
        this.moduleType = module.getType();
        this.moduleName = module.getName();
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
    public boolean ping() {
        return true;
    }

    @Override
    public synchronized boolean isAliveModule() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        return module.getLiveCycle().getStatus().isStarted();
    }

    @Override
    public synchronized final void close(){
        if (isClosed) {
            throw new IllegalStateException();
        }
        isClosed = true;
        onClose();
        module = null;
    }

    @Override
    public final boolean isClosed() {
        return isClosed;
    }

    protected abstract void onClose();

    protected final M getModule(){
        return module;
    }
}
