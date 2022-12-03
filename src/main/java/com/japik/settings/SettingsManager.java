package com.japik.settings;

import com.japik.livecycle.LiveCycleStatus;
import com.japik.logger.ILogger;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class SettingsManager implements ISettingsCallback{
    private final Settings settings;
    private final ISettingsManagerCallback managerCallback;
    @Setter @NonNull
    private ILogger logger;
    private final HashMap<String, SettingListenerContainer> keyListenerMap;
    @Nullable
    private ICommonSettingsListener commonListener = null;

    public SettingsManager(ISettingsManagerCallback managerCallback, ILogger logger) {
        settings = new Settings(this);
        this.managerCallback = Objects.requireNonNull(managerCallback);
        this.logger = Objects.requireNonNull(logger);
        keyListenerMap = new HashMap<>(0);
    }

    public SettingsManager(ISettingsManagerCallback managerCallback, ILogger logger, Settings settings) {
        this.settings = Objects.requireNonNull(settings);
        settings.setCallback(this);
        this.managerCallback = Objects.requireNonNull(managerCallback);
        this.logger = Objects.requireNonNull(logger);
        keyListenerMap = new HashMap<>(0);
    }

    public synchronized void setListener(SettingListenerContainer listenerContainer){
        keyListenerMap.put(listenerContainer.getKey(), listenerContainer);
    }

    public synchronized void removeListener(String key){
        keyListenerMap.remove(key);
    }

    public synchronized void removeAllListeners(){
        keyListenerMap.clear();
        commonListener = null;
    }

    public synchronized boolean existsListener(String key){
        return keyListenerMap.containsKey(key);
    }

    public synchronized void setCommonSettingsListener(ICommonSettingsListener listener){
        commonListener = listener;
    }

    public synchronized void removeCommonSettingsListener()
    {
        commonListener = null;
    }

    /**
     *
     * @throws SettingsApplyIncompleteException
     * @throws IllegalStateException - element not initialized or status is not final
     */
    public void apply() throws SettingsApplyIncompleteException{

        SettingListenerEventMask eventMask = SettingListenerEventMask.ON_APPLY;

        if (managerCallback.getLiveCycle().getStatus().isStarted()) {
            eventMask = eventMask.append(SettingListenerEventMask.WHEN_STARTED);
        }

        else if (managerCallback.getLiveCycle().getStatus().isInitialized()
                || managerCallback.getLiveCycle().getStatus().is(LiveCycleStatus.AdvancedNames.INITIALIZING)) {
            eventMask = eventMask.append(SettingListenerEventMask.WHEN_INITIALIZED);
        }

        else throw new IllegalStateException();

        apply(eventMask);
    }

    private synchronized void apply(SettingListenerEventMask eventMaskParts1And2) throws SettingsApplyIncompleteException {
        settings.getSettingsLocker().lock();
        try {

            managerCallback.getLiveCycle().getLiveCycleLock().lock();
            try {
                ArrayList<Throwable> throwableList = null;

                for (final String key : keyListenerMap.keySet()) {
                    try {
                        final SettingListenerContainer listenerContainer = keyListenerMap.get(key);
                        if (!listenerContainer.getEventMask().containsComplete(eventMaskParts1And2)) continue;

                        if (!settings.containsKey(key)){
                            if (listenerContainer.isOptional()) continue;
                            if (listenerContainer.getDefaultValue() == null){
                                throw new MissingSettingException(settings, key);
                            }
                            settings.put(key, listenerContainer.getDefaultValue());
                        }

                        final SettingListenerEventMask eventMask;
                        if (settings.isChanged(key)) {
                            eventMask = eventMaskParts1And2.append(SettingListenerEventMask.IS_CHANGED);
                        } else {
                            eventMask = eventMaskParts1And2.append(SettingListenerEventMask.IS_NOT_CHANGED);
                        }
                        if (!listenerContainer.getEventMask().containsComplete(eventMask)) continue;
                        listenerContainer.getListener().apply(key, settings.get(key), eventMask);

                        settings.setChanged(key, false);

                    } catch (Throwable throwable) {
                        logger.exception(throwable, "Failed apply key = " + key);
                        if (throwableList == null) {
                            throwableList = new ArrayList<>();
                        }
                        throwableList.add(throwable);
                    }
                }

                if (commonListener != null) {
                    try {
                        final SettingListenerEventMask eventMask =
                                (settings.isChanged() ?
                                        eventMaskParts1And2.append(SettingListenerEventMask.IS_CHANGED) :
                                        eventMaskParts1And2.append(SettingListenerEventMask.IS_NOT_CHANGED));
                        commonListener.apply(settings, eventMask);

                    } catch (Throwable throwable) {
                        logger.exception(throwable, "Failed apply common settings listener");
                        if (throwableList == null) {
                            throwableList = new ArrayList<>(1);
                        }
                        throwableList.add(throwable);
                    }
                }

                settings.setChanged(throwableList != null);

                if (throwableList != null) throw new SettingsApplyIncompleteException(settings, throwableList);

            } finally {
                managerCallback.getLiveCycle().getLiveCycleLock().unlock();
            }

        } finally {
            settings.getSettingsLocker().unlock();
        }
    }

    public boolean applyIfChanged() throws SettingsApplyIncompleteException {
        if (!settings.isChanged()) return false;
        apply();
        return true;
    }

    public Settings getSettings() {
        return settings;
    }

    // ISettingsCallback

    @Override
    public void onValueChanged(String key, String val) throws SettingsApplyIncompleteException {
        if (!getStatus().isFinal())
            throw new IllegalStateException();

        SettingListenerEventMask eventMask = SettingListenerEventMask.ON_CHANGE;

        if (managerCallback.getLiveCycle().getStatus().isStarted())
            eventMask = eventMask.append(SettingListenerEventMask.WHEN_STARTED);

        else if (managerCallback.getLiveCycle().getStatus().isInitialized())
            eventMask = eventMask.append(SettingListenerEventMask.WHEN_INITIALIZED);

        else throw new IllegalStateException();

        apply(eventMask);
    }

    private LiveCycleStatus getStatus() {
        return managerCallback.getLiveCycle().getStatus();
    }

    // ---------

    @FunctionalInterface
    public interface ICommonSettingsListener{
        void apply(Settings settings, SettingListenerEventMask eventMask) throws Throwable;
    }
}
