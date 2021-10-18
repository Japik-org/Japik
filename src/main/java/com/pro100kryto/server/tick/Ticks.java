package com.pro100kryto.server.tick;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.logger.Loggers;
import com.pro100kryto.server.settings.Settings;

public final class Ticks {
    private static final Tenant tenant = new Tenant(Ticks.class.getName());
    private static TickGroupCollection defaultCollection = null;

    public static TickGroupCollection getDefaultCollection(){
        checkCollection();
        return defaultCollection;
    }

    // ---

    public static ITickGroup newTickGroupFreeMod(){
        checkCollection();
        return defaultCollection.createTickGroup(new TickGroupFreeMod.Builder());
    }

    public static ITickGroup newTickGroupFreeMod(TickGroupFreeMod.BaseSettings settings){
        checkCollection();
        return defaultCollection.createTickGroup(new TickGroupFreeMod.Builder()
                .setBaseSettings(settings));
    }

    public static ITickGroup newTickGroupFreeMod(Settings settings){
        checkCollection();
        return defaultCollection.createTickGroup(new TickGroupFreeMod.Builder()
                .setBaseSettings(TickGroupFreeMod.BaseSettings.newFrom(settings)));
    }

    // ---

    public static ITickGroup newTickGroupPreMod(){
        checkCollection();
        return defaultCollection.createTickGroup(new TickGroupPreMod.Builder());
    }

    public static ITickGroup newTickGroupPreMod(TickGroupPreMod.BaseSettings settings){
        checkCollection();
        return defaultCollection.createTickGroup(new TickGroupPreMod.Builder()
                .setBaseSettings(settings));
    }

    public static ITickGroup newTickGroupPreMod(Settings settings){
        checkCollection();
        return defaultCollection.createTickGroup(new TickGroupPreMod.Builder()
                .setBaseSettings(TickGroupPreMod.BaseSettings.newFrom(settings)));
    }

    // ---

    private static void checkCollection(){
        if (defaultCollection == null){
            defaultCollection = new TickGroupCollection(tenant, Loggers.getDefaultLogger());
        }
    }
}
