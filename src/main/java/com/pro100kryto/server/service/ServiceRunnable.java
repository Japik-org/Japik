package com.pro100kryto.server.service;

import com.pro100kryto.server.StartStopStatus;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.IModule;


public class ServiceRunnable implements Runnable{
    private final Object locker = new Object();
    private final Service service;
    private final int sleeping;
    private final int threadCount;
    private final ILogger logger;
    private boolean isStarted = false;
    private Thread[] threads;

    public ServiceRunnable(Service service, int sleepBetweenTicks, int threadCount, ILogger logger){
        this.service = service;
        this.sleeping = sleepBetweenTicks;
        this.threadCount = threadCount;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (isStarted){
            synchronized (locker) {
                if (!isStarted) continue;
                try {
                    Thread.sleep(sleeping);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isStarted) continue;
            }

            Iterable<IModule> modules = service.getModules();
            for (IModule module : modules){
                try {
                    if (module.getStatus() == StartStopStatus.STARTED) module.tick();
                } catch (Throwable e) {
                    logger.writeException(e, "module "+module.getName());
                }
            }
        }
    }

    public void start(){
        if (isStarted) return;
        threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(this);
        }
        isStarted = true;

        for (int i = 0; i < threadCount; i++) {
            threads[i].start();
        }
    }

    public void stop(boolean force){
        isStarted = false;
        if (force){
            for (int i = 0; i < threadCount; i++) {
                try {
                    Thread.sleep(sleeping);
                    if (threads[i].isAlive()) threads[i].interrupt();
                } catch (Throwable ignored){
                }
            }
        }
    }

    public boolean isAlive() {
        return isStarted;
    }
}
