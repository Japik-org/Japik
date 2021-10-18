package com.pro100kryto.server.tick;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.settings.SettingsManager;
import org.jetbrains.annotations.Nullable;

public interface ITickGroup {
    long getId();
    Tenant getTenant();

    /**
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    ITick createTick(ATickRunnable tickRunnable);

    @Nullable
    ITick getTick(long id);

    /**
     * @throws IllegalStateException
     */
    void deleteTick(long id);

    ILiveCycle getLiveCycle();

    /**
     * stop, destroy and remove
     */
    void deleteTickGroup();

    SettingsManager getSettingsManager();
}
