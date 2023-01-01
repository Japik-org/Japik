package com.japik.tick;

import com.japik.dep.Tenant;
import com.japik.livecycle.ILiveCycleImpl;
import com.japik.logger.ILogger;
import com.japik.settings.SettingListenerEventMask;
import com.japik.settings.Settings;
import com.japik.settings.SettingsApplyIncompleteException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public final class TickGroupFreeMod extends ATickGroup {
    @Getter
    private BaseSettings baseSettings;

    private LongObjectHashMap<Tick> tickMap;
    private final List<Tick> tickStartedList = Collections.synchronizedList(new ArrayList<>());
    private AtomicLong tickIdCounter;

    private Thread thread;

    public TickGroupFreeMod(ITickGroupCallback tickGroupCallback,
                            long id, Tenant tenant, ILogger logger,
                            BaseSettings baseSettings,
                            @Nullable ReentrantLock liveCycleLock) {
        super(tickGroupCallback, id, tenant, logger, liveCycleLock);
        this.baseSettings = baseSettings;
        try {
            settingsManager.getSettings().put(TickGroupPreMod.BaseSettings.KEY_SLEEP_BEFORE_TICKS, baseSettings.sleepBeforeTicks);
            settingsManager.getSettings().put(TickGroupPreMod.BaseSettings.KEY_SLEEP_BETWEEN_TICKS, baseSettings.sleepBetweenTicks);
            settingsManager.getSettings().put(TickGroupPreMod.BaseSettings.KEY_SLEEP_FOR_INTERRUPT, baseSettings.sleepForInterrupt);
        } catch (SettingsApplyIncompleteException settingsApplyIncompleteException){
            logger.warn(settingsApplyIncompleteException);
        }
    }

    @Override
    public ITick createTick(ATickRunnable tickRunnable) {
        long tickId = tickIdCounter.incrementAndGet();

        if (tickMap.containsKey(id)){
            long tickIdStart = tickId;
            do {
                tickId = tickIdCounter.incrementAndGet();
                if (tickIdStart == tickId) throw new IllegalStateException("Failed create tick (group #"+this.id+"). No more id available.");
            } while (tickMap.containsKey(id));
        }

        final Tick tick = new Tick(
                id,
                this,
                logger,
                tickRunnable);
        tickMap.put(tick.getId(), tick);
        return tick;
    }

    @Override
    @Nullable
    public ITick getTick(long id){
        return tickMap.get(id);
    }

    @Override
    public void deleteTick(long id) throws IllegalStateException{
        final Tick tick = tickMap.get(id);
        if (tick.getStatus() != TickStatus.DESTROYED){
            tick.destroy();
        }
        tickMap.remove(id);
    }

    @Override
    public void deleteTickGroup() {
        tickGroupCallback.deleteTickGroup(id);
    }

    // ITickCallback

    @Override
    public void setTickActive(long id) throws IllegalArgumentException{
        final Tick tick = tickMap.get(id);
        if (tick == null) throw new IllegalArgumentException("Failed activate tick (group #"+this.id+"). Tick #"+id+" not found");
        synchronized (tickStartedList) {
            tickStartedList.add(tick);
        }
    }

    @Override
    public void setTickInactive(long id) throws IllegalArgumentException{
        final Tick tick = tickMap.get(id);
        if (tick == null) throw new IllegalArgumentException("Failed inactivate tick (group #"+this.id+"). Tick #"+id+" not found");
        synchronized (tickStartedList) {
            tickStartedList.remove(tick);
        }
    }

    // LiveCycle

    @Override
    protected ILiveCycleImpl getDefaultLiveCycleImpl() {
        return new LiveCycleImpl(this);
    }

    private final class LiveCycleImpl implements ILiveCycleImpl {
        private final TickGroupFreeMod tickGroupFreeMod;

        private LiveCycleImpl(TickGroupFreeMod tickGroupFreeMod) {
            this.tickGroupFreeMod = tickGroupFreeMod;
        }

        @Override
        public void init() throws SettingsApplyIncompleteException {
            tickMap = new LongObjectHashMap<>();
            synchronized (tickStartedList) {
                tickStartedList.clear();
            }
            tickIdCounter = new AtomicLong(0);

            settingsManager.setCommonSettingsListener((settings, eventMask) -> {
                if (eventMask.containsPartially(SettingListenerEventMask.ON_APPLY)){
                    baseSettings = BaseSettings.newFrom(settings);
                }
            });
            settingsManager.apply();
        }

        @Override
        public void start() {
            thread = new TickGroupThread(tickGroupFreeMod);
            thread.start();
        }

        @Override
        public void stopSlow() {
            thread.interrupt();
        }

        @Override
        public void stopForce() {
            if (thread!=null && thread.isAlive()) {
                thread.interrupt();
                if (thread.isAlive())
                    thread.stop();
            }
            thread = null;
        }

        @Override
        public void destroy() {
            settingsManager.removeAllListeners();

            synchronized (tickStartedList) {
                tickStartedList.clear();
            }
            tickMap = null;
            tickIdCounter = null;
        }

        @Override
        public void announceStop() {
        }

        @Override
        public boolean canBeStoppedSafe() {
            return tickStartedList.isEmpty();
        }
    }

    // thread

    private static final class TickGroupThread extends Thread{
        private final TickGroupFreeMod tickGroupFreeMod;

        public TickGroupThread(TickGroupFreeMod tickGroupFreeMod) {
            super(() -> {
                while (!currentThread().isInterrupted()) {
                    try {
                        while (!currentThread().isInterrupted()) {
                            Thread.sleep(tickGroupFreeMod.baseSettings.getSleepBeforeTicks());
                            synchronized (tickGroupFreeMod.tickStartedList) {
                                for (final Tick tick : tickGroupFreeMod.tickStartedList) {
                                    tick.tick();
                                    Thread.sleep(tickGroupFreeMod.baseSettings.getSleepBetweenTicks());
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        tickGroupFreeMod.logger.exception(throwable, "TickGroup thread exception");
                    }
                }
            });
            this.tickGroupFreeMod = tickGroupFreeMod;
        }
    }

    @RequiredArgsConstructor
    public static final class BaseSettings{
        public static final BaseSettings DEFAULT = new BaseSettings(
                0,
                0,
                1500
        );
        public static final String KEY_SLEEP_BEFORE_TICKS = "tickGroup-sleepBeforeTicks";
        public static final String KEY_SLEEP_BETWEEN_TICKS = "tickGroup-sleepBetweenTicks";
        public static final String KEY_SLEEP_FOR_INTERRUPT = "tickGroup-sleepForInterrupt";

        @Getter
        private final long sleepBeforeTicks;
        @Getter
        private final long sleepBetweenTicks;
        @Getter
        private final long sleepForInterrupt;

        public static BaseSettings newFrom(Settings settings){
            return new BaseSettings(
                    settings.getLongOrDefault(KEY_SLEEP_BEFORE_TICKS, DEFAULT.getSleepBeforeTicks()),
                    settings.getLongOrDefault(KEY_SLEEP_BETWEEN_TICKS, DEFAULT.getSleepBetweenTicks()),
                    settings.getLongOrDefault(KEY_SLEEP_FOR_INTERRUPT, DEFAULT.getSleepForInterrupt())
            );
        }
    }

    @Getter @Setter @Accessors(chain = true)
    public static final class Builder extends TickGroupBuilder{
        @NonNull
        private BaseSettings baseSettings = BaseSettings.DEFAULT;

        @Override
        public ITickGroup build(ITickGroupCallback tickGroupCallback, long id, Tenant tenant) {
            return new TickGroupFreeMod(
                    tickGroupCallback,
                    id,
                    tenant,
                    logger,
                    baseSettings,
                    liveCycleLock
            );
        }
    }
}
