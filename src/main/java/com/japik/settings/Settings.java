package com.japik.settings;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Settings {
    protected final HashMap<String, SettingContainer> map;
    @Setter @Nullable
    protected ISettingsCallback callback = null;
    private final ReentrantLock locker = new ReentrantLock();
    protected boolean changed = true;

    public Settings(@Nullable ISettingsCallback callback, int initialCapacity) {
        map = new HashMap<>(initialCapacity);
        this.callback = callback;
    }

    public Settings(ISettingsCallback callback) {
        map = new HashMap<>();
        this.callback = Objects.requireNonNull(callback);;
    }

    public Settings() {
        map = new HashMap<>();
    }

    public void apply() throws SettingsApplyIncompleteException {
        callback.apply();
    }

    public boolean applyIfChanged() throws SettingsApplyIncompleteException {
        return callback.applyIfChanged();
    }

    public String put(String key, String value) throws SettingsApplyIncompleteException {
        locker.lock();
        final String oldVal;

        try {
            if (map.containsKey(key)) {
                final SettingContainer container = map.get(key);
                oldVal = container.getVal();
                container.setVal(value);
            } else {
                map.put(key, new SettingContainer(key, value));
                oldVal = null;
            }
            changed = true;
            try {
                callback.onValueChanged(key, value);
            } catch (IllegalStateException ignored){
            }

        } finally {
            locker.unlock();
        }

        return oldVal;
    }

    public void clear() {
        locker.lock();
        map.clear();
        changed = true;
        locker.unlock();
    }

    /**
     * @return value
     * @throws NullPointerException
     */
    public String get(String key){
        locker.lock();
        try{
            return map.get(key).getVal();

        } finally {
            locker.unlock();
        }
    }

    public int size(){
        locker.lock();
        try{
            return map.size();

        } finally {
            locker.unlock();
        }
    }

    public String getOrDefault(String key, String defaultValue){
        locker.lock();
        try{
            final SettingContainer container = map.get(key);
            if (container == null) return defaultValue;
            return container.getVal();

        } finally {
            locker.unlock();
        }
    }

    public boolean containsKey(String key){
        locker.lock();
        try{
            return map.containsKey(key);

        } finally {
            locker.unlock();
        }
    }

    public boolean isChanged() {
        locker.lock();
        try{
            return changed;

        } finally {
            locker.unlock();
        }
    }

    public boolean isChanged(String key) {
        locker.lock();
        try {
            return map.get(key).isChanged();

        } finally {
            locker.unlock();
        }
    }

    public void setChanged(String key, boolean changed) {
        locker.lock();
        try {
            map.get(key).setChanged(changed);

        } finally {
            locker.unlock();
        }
    }

    public void setChanged(boolean changed) {
        locker.lock();
        try {
            this.changed = changed;

        } finally {
            locker.unlock();
        }
    }

    public ReentrantLock getSettingsLocker() {
        return locker;
    }

    // boolean

    public void put(String key, boolean val) throws SettingsApplyIncompleteException {
        put(key, Boolean.toString(val));
    }

    /**
     * @throws NullPointerException
     */
    public boolean getBoolean(String key){
        return Boolean.parseBoolean( get(key) );
    }

    public boolean getBooleanOrDefault(String key, boolean defaultValue){
        return Boolean.parseBoolean(
                getOrDefault(key, Boolean.toString(defaultValue)));
    }

    // byte

    public void put(String key, byte val) throws SettingsApplyIncompleteException {
        put(key, Byte.toString(val));
    }

    /**
     * @throws NumberFormatException
     * @throws NullPointerException
     */
    public byte getByte(String key){
        return Byte.parseByte( get(key) );
    }

    public byte getByteOrDefault(String key, byte defaultValue){
        try {
            return Byte.parseByte(
                    getOrDefault(key, Byte.toString(defaultValue)));
        } catch (NumberFormatException ignored){
        }
        return defaultValue;
    }

    // int

    public void put(String key, int val) throws SettingsApplyIncompleteException {
        put(key, Integer.toString(val));
    }

    /**
     * @throws NumberFormatException
     * @throws NullPointerException
     */
    public int getInt(String key){
        return Integer.parseInt( get(key) );
    }

    public int getIntOrDefault(String key, int defaultValue){
        try {
            return Integer.parseInt(
                    getOrDefault(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException ignored){
        }
        return defaultValue;
    }

    // long

    public void put(String key, long val) throws SettingsApplyIncompleteException {
        put(key, Long.toString(val));
    }

    /**
     * @throws NumberFormatException
     * @throws NullPointerException
     */
    public long getLong(String key){
        return Long.parseLong( get(key) );
    }

    public long getLongOrDefault(String key, long defaultValue){
        try {
            return Long.parseLong(
                    getOrDefault(key, Long.toString(defaultValue)));
        } catch (NumberFormatException ignored){
        }
        return defaultValue;
    }

    // double

    public void put(String key, double val) throws SettingsApplyIncompleteException {
        put(key, Double.toString(val));
    }

    /**
     * @throws NumberFormatException
     * @throws NullPointerException
     */
    public double getDouble(String key){
        return Double.parseDouble( get(key) );
    }

    public double getDoubleOrDefault(String key, double defaultValue){
        try {
            return Double.parseDouble(
                    getOrDefault(key, Double.toString(defaultValue)));
        } catch (NumberFormatException ignored){
        }
        return defaultValue;
    }

    // enum

    public <E extends Enum<E>> void put(String key, E val) throws SettingsApplyIncompleteException {
        put(key, val.toString());
    }

    /**
     * @throws NullPointerException - no value exists for key
     * @throws IllegalArgumentException - if the specified enum type has no constant with the specified name, or the specified class object does not represent an enum type
     */
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key){
        return E.valueOf(clazz, get(key) );
    }

    public <E extends Enum<E>> E getEnumOrDefault(Class<E> clazz, String key, E defaultValue){
        try {
            return E.valueOf(clazz, getOrDefault(key, defaultValue.toString()));
        } catch (IllegalArgumentException ignore){
        }
        return defaultValue;
    }

    // -------


    @Override
    public String toString() {
        return super.toString();
    }

    public HashMap<String, SettingContainer> getMap() {
        return map;
    }
}
