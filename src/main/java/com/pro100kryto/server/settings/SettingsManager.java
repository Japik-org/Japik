package com.pro100kryto.server.settings;

import com.pro100kryto.server.livecycle.ILiveCycleStatusContainer;
import com.pro100kryto.server.logger.ILogger;

import java.util.ArrayList;
import java.util.HashMap;

public final class SettingsManager implements ISettingsCallback{
    private final ISettingsManagerCallback managerCallback;
    private final ILogger logger;
    private final Settings settings;
    private final HashMap<String, SettingListenerContainer> keyListenerMap;
    private ICommonSettingsListener commonListener = null;

    public SettingsManager(ISettingsManagerCallback managerCallback, ILogger logger) {
        this.managerCallback = managerCallback;
        this.logger = logger;
        settings = new Settings(this);
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
        if (!getStatus().isFinal())
            throw new IllegalStateException();

        SettingListenerEventMask eventMask = SettingListenerEventMask.ON_APPLY;

        if (managerCallback.getLiveCycle().getStatus().isStarted())
            eventMask = eventMask.append(SettingListenerEventMask.WHEN_STARTED);

        else if (managerCallback.getLiveCycle().getStatus().isInitialized())
            eventMask = eventMask.append(SettingListenerEventMask.WHEN_INITIALIZED);

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

                        if (settings.containsKey(key)) {
                            SettingListenerEventMask eventMask;
                            if (settings.isChanged(key)) {
                                eventMask = eventMaskParts1And2.append(SettingListenerEventMask.IS_CHANGED);
                            } else {
                                eventMask = eventMaskParts1And2.append(SettingListenerEventMask.IS_NOT_CHANGED);
                            }
                            if (!listenerContainer.getEventMask().containsComplete(eventMask)) continue;
                            listenerContainer.getListener().apply(key, settings.get(key), eventMask);

                        } else {
                            if (listenerContainer.isOptional()) continue;
                            throw new MissingSettingException(settings, key);
                        }

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

                settings.setChanged(throwableList == null);

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

    @Override
    public ILiveCycleStatusContainer getStatus() {
        return managerCallback.getLiveCycle().getStatus();
    }

    // ---------

    @FunctionalInterface
    public interface ICommonSettingsListener{
        void apply(Settings settings, SettingListenerEventMask eventMask) throws Throwable;
    }
}
