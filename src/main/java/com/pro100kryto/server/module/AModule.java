package com.pro100kryto.server.module;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.*;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.IServiceConnection;
import com.pro100kryto.server.service.IServiceConnectionSafe;
import com.pro100kryto.server.settings.*;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AModule <MC extends IModuleConnection> implements IModule<MC>,
        ISettingsManagerCallback, IModuleConnectionCallback {

    protected final IService<?> service;
    private final Tenant tenant;
    protected final String type;
    protected final String name;
    protected final ILogger logger;

    private final LiveCycleController liveCycleController, liveCycleControllerInternal;

    protected final Settings settings;
    protected final SettingsManager settingsManager;
    protected final BaseModuleSettings baseSettings;

    private boolean moduleConnectionMultipleEnabled;
    private int moduleConnectionMultipleMaxCount;
    private IntObjectHashMap<MC> moduleConnectionMap;
    private AtomicInteger moduleConnectionCounter;

    public AModule(ModuleParams moduleParams){
        service = moduleParams.getService();
        type = moduleParams.getModuleType();
        name = moduleParams.getModuleName();
        logger = moduleParams.getLogger();
        tenant = moduleParams.getModuleAsTenant();

        // live cycle
        // 3
        liveCycleController = new LiveCycleController.Builder()
                .setDefaultImpl(createDefaultLiveCycleImpl()) // 4
                .build(logger, "Module name='"+name+"'");

        // 1
        liveCycleControllerInternal = new LiveCycleController.Builder()
                .setDefaultImpl(new AModuleLiveCycleInternalImpl()) // 2
                .build(logger, "Module (internal) name='"+name+"'");

        // settings
        settings = new Settings();
        settingsManager = new SettingsManager(settings, this, logger);
        baseSettings = new BaseModuleSettings(settingsManager.getSettings());
    }

    @Override
    public final IService<?> getService() {
        return service;
    }

    @Override
    public final String getType() {
        return type;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Settings getSettings() {
        return settingsManager.getSettings();
    }

    @Override
    public final ModuleConnectionSafeFromService<MC> getModuleConnectionSafe() {
        return new ModuleConnectionSafeFromService<>(service, this.name);
    }

    @Override
    public final Tenant asTenant() {
        return tenant;
    }

    @Override
    public final String toString() {
        return "Module { type:'"+type+"', name:'"+name+"', serviceName:'"+service.getName()+"' }";
    }

    @NotNull
    public final MC getModuleConnection() throws RemoteException {
        if (getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException("Module is not initialized");
        }

        if (moduleConnectionMultipleEnabled || moduleConnectionMap.isEmpty()){
            try {
                return _createModuleConnection();

            } catch (RemoteException remoteException){
                throw remoteException;

            } catch (Throwable throwable){
                throw new ModuleConnectionException(
                        service.getName(),
                        getName(),
                        throwable
                );
            }
        }

        return moduleConnectionMap.get(moduleConnectionCounter.get());
    }

    private MC _createModuleConnection() throws Throwable {
        if (moduleConnectionMap.size() >= moduleConnectionMultipleMaxCount){
            throw new IllegalStateException("No more space for connections");
        }
        final int connId = moduleConnectionCounter.incrementAndGet();
        final MC mc = createModuleConnection(new ModuleConnectionParams(
                connId,
                logger,
                this
        ));

        moduleConnectionMap.put(connId, mc);
        return mc;
    }

    // virtual

    @NotNull
    protected ILiveCycleImpl createDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }

    protected void setupLiveCycleControllerBeforeInit(LiveCycleController liveCycleController){
    }

    protected void setupSettingsBeforeInit() throws Throwable {
        settingsManager.setListener(new SettingListenerContainer(
                BaseModuleSettings.KEY_AUTO_FIX_BROKEN_ENABLED,
                new BooleanSettingListener() {
                    @Override
                    public void apply2(String key, Boolean val, SettingListenerEventMask eventMask) {
                        liveCycleController.setEnabledAutoFixBroken(val);
                    }
                },
                Boolean.toString(false)
        ));

        if (baseSettings.isConnectionMultipleEnabled()) {
            settingsManager.setListener(new SettingListenerContainer(
                    BaseModuleSettings.KEY_CONNECTION_MULTIPLE_COUNT,
                    new IntegerSettingListener() {
                        @Override
                        public void apply2(String key, Integer val, SettingListenerEventMask eventMask) {
                            final IntObjectHashMap<MC> newMap = new IntObjectHashMap<>(val);
                            newMap.putAll(moduleConnectionMap);
                            moduleConnectionMap = newMap;
                            moduleConnectionMultipleMaxCount = val;
                        }
                    }
            ));
        }
    }

    @NotNull
    protected abstract MC createModuleConnection(ModuleConnectionParams params) throws Throwable;

    @Override
    public void onCloseModuleConnection(int id) {
        moduleConnectionMap.remove(id);
    }

    // utils

    protected final void startModuleOrThrow(String moduleName) throws ModuleNotFoundException, InitException, StartException {
        final IModule<?> module = service.getModuleLoader().getModule(moduleName);
        if (!module.getLiveCycle().getStatus().isInitialized()){
            module.getLiveCycle().init();
        }
        if (!module.getLiveCycle().getStatus().isStarted()){
            module.getLiveCycle().start();
        }
    }

    protected final void startModuleOrWarn(String moduleName) {
        try {
            startModuleOrThrow(moduleName);

        } catch (Throwable throwable) {
            logger.warn("Failed start module name='"+moduleName+"'", throwable);
        }
    }

    protected final void initModuleOrThrow(String moduleName) throws ModuleNotFoundException, InitException {
        final IModule<?> module = service.getModuleLoader().getModule(moduleName);
        if (!module.getLiveCycle().getStatus().isInitialized()){
            module.getLiveCycle().init();
        }
    }

    protected final void initModuleOrWarn(String moduleName) {
        try {
            initModuleOrThrow(moduleName);

        } catch (Throwable throwable) {
            logger.warn("Failed init module name='"+moduleName+"'", throwable);
        }
    }

    protected final <T extends IModuleConnection>
    IModuleConnectionSafe<T> setupModuleConnectionSafe(String moduleName){

        final IModuleConnectionSafe<T> moduleConnectionSafe =
                service.createModuleConnectionSafe(moduleName);

        if (!moduleConnectionSafe.isAliveConnection()) {
            try {
                moduleConnectionSafe.refreshConnection();
            } catch (Throwable throwable) {
                logger.warn("Failed establish connection with module name='"+moduleName+"'", throwable);
            }
        }

        return moduleConnectionSafe;
    }

    @Nullable
    protected final <T extends IModuleConnection>
    IModuleConnectionSafe<T> closeModuleConnection(@Nullable final IModuleConnectionSafe<T> moduleConnectionSafe) {
        if (moduleConnectionSafe != null && !moduleConnectionSafe.isClosed()) {
            try {
                moduleConnectionSafe.close();
            } catch (IllegalStateException ignored){
            }
        }
        return null;
    }

    protected final <T extends IServiceConnection>
    IServiceConnectionSafe<T> setupServiceConnectionSafe(String serviceName){

        final IServiceConnectionSafe<T> serviceConnectionSafe =
                service.getCallback().createServiceConnectionSafe(serviceName);

        if (!serviceConnectionSafe.isAliveConnection()) {
            try {
                serviceConnectionSafe.refreshConnection();
            } catch (Throwable throwable) {
                logger.warn("Failed establish connection with service name='"+serviceName+"'", throwable);
            }
        }

        return serviceConnectionSafe;
    }

    @Nullable
    protected final <T extends IServiceConnection>
    IServiceConnectionSafe<T> closeServiceConnection(@Nullable final IServiceConnectionSafe<T> serviceConnectionSafe) {
        if (serviceConnectionSafe != null && !serviceConnectionSafe.isClosed()) {
            try {
                serviceConnectionSafe.close();
            } catch (IllegalStateException ignored){
            }
        }
        return null;
    }

    // LiveCycle

    @Override
    public final ILiveCycle getLiveCycle() {
        return liveCycleControllerInternal;
    }

    private final class AModuleLiveCycleInternalImpl implements ILiveCycleImpl {

        @Override
        public void init() throws Throwable {
            setupSettingsBeforeInit();

            setupLiveCycleControllerBeforeInit(liveCycleController);
            liveCycleController.init();
            settingsManager.applyIfChanged();

            // connection

            moduleConnectionMultipleEnabled = baseSettings.isConnectionMultipleEnabled();
            moduleConnectionMultipleMaxCount = (baseSettings.isConnectionMultipleEnabled() ? baseSettings.getConnectionMultipleCount() : 1);

            moduleConnectionMap = new IntObjectHashMap<>(moduleConnectionMultipleMaxCount);
            moduleConnectionCounter = new AtomicInteger();

            if (baseSettings.isConnectionCreateAfterInitEnabled()){
                _createModuleConnection();
            }
        }

        @Override
        public void start() throws Throwable {
            settingsManager.applyIfChanged();
            liveCycleController.start();
        }

        @Override
        public void stopSlow() throws Throwable {
            liveCycleController.stopSlow();
        }

        @Override
        public void stopForce() {
            liveCycleController.stopForce();
        }

        @Override
        public void destroy() {
            settingsManager.removeAllListeners();

            while (!moduleConnectionMap.isEmpty()){
                final MC mc = moduleConnectionMap.iterator().next();
                mc.close();
                moduleConnectionMap.remove(mc.getId());
            }

            liveCycleController.destroy();
            liveCycleController.setDefaultImpl();
        }

        @Override
        public void announceStop() {
            liveCycleController.announceStop();
        }

        @Override
        public boolean canBeStoppedSafe() {
            return liveCycleController.canBeStoppedSafe();
        }
    }
}
