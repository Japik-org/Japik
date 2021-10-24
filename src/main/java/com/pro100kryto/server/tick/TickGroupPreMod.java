package com.pro100kryto.server.tick;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.settings.SettingListenerEventMask;
import com.pro100kryto.server.settings.Settings;
import com.pro100kryto.server.settings.SettingsApplyIncompleteException;
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
        return new LiveCycleImpl();
    }

    private final class LiveCycleImpl implements ILiveCycleImpl {

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
            thread = new TickGroupThread();
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

    private final class TickGroupThread extends Thread{

        public TickGroupThread() {
            super(() -> {
                try {
                    final Tick[] tickArr = tickStartedList.toArray(new Tick[0]);
                    final int tickArrLen = tickArr.length;

                    while (!liveCycleController.getStatus().isStopAnnounced()) {
                        Thread.sleep(baseSettings.getSleepBeforeTicks());
                        for (int i = 0; i < tickArrLen; i++) {
                            final Tick tick = tickArr[i];
                            tick.tick();
                            Thread.sleep(baseSettings.getSleepBetweenTicks());
                        }
                    }
                } catch (Throwable throwable){
                    logger.exception(throwable,"TickGroup thread exception");
                    try {
                        liveCycleController.stopForce();
                    } catch (IllegalStateException illegalStateException){
                        logger.exception(illegalStateException, "stopForce() failed after TickGroup thread crash");
                    }
                }
            });
        }
    }

    @RequiredArgsConstructor
    public static final class BaseSettings{
        private static final BaseSettings DEFAULT = new BaseSettings(
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
