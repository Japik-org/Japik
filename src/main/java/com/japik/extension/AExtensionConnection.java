package com.japik.extension;

import com.japik.livecycle.LiveCycleStatus;
import com.japik.logger.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AExtensionConnection<E extends IExtension<EC>, EC extends IExtensionConnection>
    implements IExtensionConnection {

    @Nullable
    protected E extension;
    protected final IExtensionConnectionCallback callback;
    protected final String extensionType;
    protected final ILogger logger;

    private boolean isClosed = false;

    public AExtensionConnection(@NotNull E extension, ExtensionConnectionParams params) {
        this.extension = Objects.requireNonNull(extension);
        this.callback = Objects.requireNonNull(params.getCallback());
        this.extensionType = extension.getType();
        this.logger = Objects.requireNonNull(params.getLogger());
    }

    @Nullable
    protected final E getExtension() {
        return extension;
    }

    @Override
    public final String getExtensionType() {
        return extensionType;
    }

    public final LiveCycleStatus getExtensionStatus() {
        return extension.getLiveCycle().getStatus();
    }

    @Override
    public synchronized final void close() {
        if (isClosed) {
            throw new IllegalStateException("Extension type='"+extensionType+"' is already closed.");
        }
        isClosed = true;
        try {
            onClose();
        } finally {
            extension = null;
        }

        callback.onCloseExtensionConnection(extensionType);
    }

    @Override
    public final boolean isClosed() {
        return isClosed;
    }

    protected void onClose(){}
}
