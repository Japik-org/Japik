package com.pro100kryto.server.tick;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.logger.ILogger;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public final class TickGroupCollection implements ITickGroupCallback {
    private final Tenant tenant;
    private final LongObjectHashMap<ITickGroup> idTickGroupMap = new LongObjectHashMap<>(); // TODO: long initialization?
    private final ReentrantLock mapLock = new ReentrantLock();
    private final ILogger logger;
    private final AtomicLong groupIdCounter;

    public TickGroupCollection(Tenant tenant, ILogger logger) {
        this.tenant = Objects.requireNonNull(tenant);
        this.logger = Objects.requireNonNull(logger);
        groupIdCounter = new AtomicLong(0);
    }

    public ITickGroup createTickGroup(TickGroupBuilder tickGroupBuilder){
        final ITickGroup tickGroup = tickGroupBuilder.build(
                this,
                groupIdCounter.incrementAndGet(),
                tenant
        );

        mapLock.lock();
        try {
            idTickGroupMap.put(tickGroup.getId(), tickGroup);

        } finally {
            mapLock.unlock();
        }

        return tickGroup;
    }

    @Nullable
    public ITickGroup getTickGroup(long id){
        mapLock.lock();
        try {
            return idTickGroupMap.get(id);

        } finally {
            mapLock.unlock();
        }
    }

    public boolean existsTickGroup(long id) {
        mapLock.lock();
        try {
            return idTickGroupMap.contains(id);

        } finally {
            mapLock.unlock();
        }
    }

    @Override
    public boolean deleteTickGroup(long id){
        mapLock.lock();
        try {
            final ITickGroup tickGroup = idTickGroupMap.get(id);
            if (tickGroup == null) return false;

            if (tickGroup.getLiveCycle().getStatus().isStarted()) {
                tickGroup.getLiveCycle().stopForce();
            }
            if (tickGroup.getLiveCycle().getStatus().isInitialized()) {
                tickGroup.getLiveCycle().destroy();
            }

            idTickGroupMap.remove(id);
            return true;

        } finally {
            mapLock.unlock();
        }
    }

    public boolean deleteTickGroups() {
        mapLock.lock();
        try {

            for (final ITickGroup tickGroup: idTickGroupMap.values()){
                if (tickGroup.getLiveCycle().getStatus().isStarted()) tickGroup.getLiveCycle().stopForce();
                if (tickGroup.getLiveCycle().getStatus().isInitialized()) tickGroup.getLiveCycle().destroy();
                idTickGroupMap.remove(tickGroup.getId());
            }
            idTickGroupMap.clear();
            return true;

        } finally {
            mapLock.unlock();
        }
    }

    @Nullable
    public Iterable<ITickGroup> getTickGroups(){
        mapLock.lock();
        try {
            return idTickGroupMap.values();
        } finally {
            mapLock.unlock();
        }
    }

    public void destroyTickGroups(){
        mapLock.lock();
        try {
            for (final ITickGroup tickGroup : idTickGroupMap) {
                try {
                    tickGroup.getLiveCycle().destroy();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

        } finally {
            mapLock.unlock();
        }
    }

    public void destroyAndClear(){
        mapLock.lock();
        try {
            destroyTickGroups();
            idTickGroupMap.clear();
        } finally {
            mapLock.unlock();
        }
    }
}
