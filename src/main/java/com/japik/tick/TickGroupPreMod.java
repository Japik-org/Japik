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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TickGroupPreMod extends ATickGroup {
    @Getter
    private BaseSettings baseSettings;

    private LongObjectHashMap<Tick> tickMap;
    private ArrayList<Tick> tickStartedList;
    private AtomicLong tickIdCounter;

    private Thread thread;

    protected TickGroupPreMod(ITickGroupCallback tickGroupCallback,
                           long id, Tenant tenant, ILogger logger,
                           BaseSettings baseSettings,
                           @Nullable ReentrantLock liveCycleLock) {
        super(tickGroupCallback, id, tenant, logger, liveCycleLock);
        this.baseSettings = baseSettings;
        try {
            settingsManager.getSettings().put(BaseSettings.KEY_SLEEP_BEFORE_TICKS, baseSettings.sleepBeforeTicks);
            settingsManager.getSettings().put(BaseSettings.KEY_SLEEP_BETWEEN_TICKS, baseSettings.sleepBetweenTicks);
            settingsManager.getSettings().put(BaseSettings.KEY_SLEEP_FOR_INTERRUPT, baseSettings.sleepForInterrupt);
        } catch (SettingsApplyIncompleteException settingsApplyIncompleteException){
            logger.warn(settingsApplyIncompleteException);
        }
    }

    @Override
    public ITick createTick(ATickRunnable tickRunnable) {
        if (liveCycleController.getStatus().isStarted()) throw new IllegalStateException();

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
    public void deleteTick(long id){
        if (liveCycleController.getStatus().isStarted()) throw new IllegalStateException();

        final Tick tick = tickMap.get(id);
        if (tick.getStatus() != TickStatus.DESTROYED){
            tick.destroy();
        }
        tickMap.remove(id);
    }

    @Override
    public void deleteTickGroup() {
        if (liveCycleController.getStatus().isStarted()) throw new IllegalStateException();
        tickGroupCallback.deleteTickGroup(id);
    }

    // ITickCallback

    @Override
    public void setTickActive(long id){
        if (liveCycleController.getStatus().isStarted()) throw new IllegalStateException();

        final Tick tick = tickMap.get(id);
        if (tick == null) throw new IllegalArgumentException("Failed activate tick (group #"+this.id+"). Tick #"+id+" not found");
        tickStartedList.add(tick);
    }

    @Override
    public void setTickInactive(long id){
        if (liveCycleController.getStatus().isStarted()) throw new IllegalStateException();

        final Tick tick = tickMap.get(id);
        if (tick == null) throw new IllegalArgumentException("Failed inactivate tick (group #"+this.id+"). Tick #"+id+" not found");
        tickStartedList.remove(tick);
    }

    // LiveCycle

    @Override
    protected ILiveCycleImpl getDefaultLiveCycleImpl() {
        return new LiveCycleImpl(this);
    }

    private final class LiveCycleImpl implements ILiveCycleImpl {
        private final TickGroupPreMod tickGroupPreMod;

        private LiveCycleImpl(TickGroupPreMod tickGroupPreMod) {
            this.tickGroupPreMod = tickGroupPreMod;
        }

        @Override
        public void init() throws SettingsApplyIncompleteException {
            tickMap = new LongObjectHashMap<>();
            tickStartedList = new ArrayList<>();
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
            thread = new TickGroupThread(tickGroupPreMod);
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

            tickMap = null;
            tickStartedList = null;
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
        private final TickGroupPreMod tickGroupPreMod;

        public TickGroupThread(TickGroupPreMod tickGroupPreMod) {
            super(() -> {
                try {
                    final Tick[] tickArr = tickGroupPreMod.tickStartedList.toArray(new Tick[0]);
                    final int tickArrLen = tickArr.length;

                    while (!tickGroupPreMod.liveCycleController.getStatus().isStopAnnounced()) {
                        Thread.sleep(tickGroupPreMod.baseSettings.getSleepBeforeTicks());
                        for (int i = 0; i < tickArrLen; i++) {
                            final Tick tick = tickArr[i];
                            tick.tick();
                            Thread.sleep(tickGroupPreMod.baseSettings.getSleepBetweenTicks());
                        }
                    }
                } catch (Throwable throwable){
                    tickGroupPreMod.logger.exception(throwable,"TickGroup thread exception");
                    try {
                        tickGroupPreMod.liveCycleController.stopForce();
                    } catch (IllegalStateException illegalStateException){
                        tickGroupPreMod.logger.exception(illegalStateException, "stopForce() failed after TickGroup thread crash");
                    }
                }
            });
            this.tickGroupPreMod = tickGroupPreMod;
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
    public static final class Builder extends TickGroupBuilder {
        @NonNull
        private BaseSettings baseSettings = BaseSettings.DEFAULT;

        @Override
        public ITickGroup build(ITickGroupCallback tickGroupCallback, long id, Tenant tenant) {
            return new TickGroupPreMod(
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
