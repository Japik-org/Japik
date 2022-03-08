package com.pro100kryto.server.element;

import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.controller.ILiveCycleImplId;
import com.pro100kryto.server.livecycle.controller.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.BaseModuleSettings;
import com.pro100kryto.server.settings.*;
import lombok.Getter;
import lombok.Setter;

public abstract class AElement implements IElement, ISettingsManagerCallback {
    protected final ElementType elementType;
    protected final String subtype;
    protected final String name;
    protected final Tenant tenant;
    protected final ILogger logger;

    protected final LiveCycleController liveCycleController;

    protected final Settings settings;
    protected final SettingsManager settingsManager;

    public AElement(ElementType elementType, String subtype, String name, Tenant tenant, ILogger logger) {
        this.elementType = elementType;
        this.subtype = subtype;
        this.name = name;
        this.tenant = tenant;
        this.logger = logger;

        liveCycleController = new LiveCycleController.Builder()
                .setElementName("Element type='"+elementType+"' subtype='"+subtype+"' name='"+name+"'")
                .setDefaultImpl(new DefaultLiveCycleImpl())
                .setLogger(this.logger)
                .build();

        settings = new Settings();
        settingsManager = new SettingsManager(settings, this, logger);
    }

    @Override
    public final ElementType getElementType() {
        return elementType;
    }

    @Override
    public final String getType() {
        return subtype;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Tenant getTenant() {
        return tenant;
    }

    @Override
    public final ILiveCycle getLiveCycle() {
        return liveCycleController;
    }

    @Override
    public final Settings getSettings() {
        return settings;
    }

    // virtual

    protected void initLiveCycleController(LiveCycleController liveCycleController) {
        liveCycleController.putImplAll(new LowElementLiveCycleImpl());
    }

    @Override
    public String toString() {
        return "Element type='"+elementType+"' subtype='"+subtype+"' name='"+name+"'";
    }

    private final class DefaultLiveCycleImpl extends EmptyLiveCycleImpl {
        @Override
        public void init() throws Throwable {
            initLiveCycleController(liveCycleController);
        }
    }

    protected final class LowElementLiveCycleImpl extends EmptyLiveCycleImpl implements ILiveCycleImplId {
        @Getter
        private final String name = "Element::LOW";
        @Getter @Setter
        private int priority = LiveCycleController.PRIORITY_LOW;

        @Override
        public void init() throws Throwable {
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
        }

        @Override
        public void start() throws Throwable {
            settingsManager.applyIfChanged();
        }
    }
}
