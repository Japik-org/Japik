package com.japik.tick;

import com.japik.dep.Tenant;
import com.japik.livecycle.ILiveCycle;
import com.japik.settings.SettingsManager;
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
