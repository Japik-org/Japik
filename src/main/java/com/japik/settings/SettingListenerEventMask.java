package com.japik.settings;

public final class SettingListenerEventMask {
    private final int val;

    // part 1
    public static final SettingListenerEventMask ON_APPLY = new SettingListenerEventMask(1 << 1);
    public static final SettingListenerEventMask ON_CHANGE = new SettingListenerEventMask(1 << 2);

    // part 2
    public static final SettingListenerEventMask WHEN_INITIALIZED = new SettingListenerEventMask(1 << 3);
    public static final SettingListenerEventMask WHEN_STARTED = new SettingListenerEventMask(1 << 4);

    // part 3
    public static final SettingListenerEventMask IS_CHANGED = new SettingListenerEventMask(1 << 5);
    public static final SettingListenerEventMask IS_NOT_CHANGED = new SettingListenerEventMask(1 << 6);

    // ---
    public static final SettingListenerEventMask EMPTY = new SettingListenerEventMask(0);
    public static final SettingListenerEventMask DEFAULT = build(
            // part 1
            ON_APPLY,
            // part 2
            WHEN_INITIALIZED,
            WHEN_STARTED,
            // part 3
            IS_CHANGED
    );

    private SettingListenerEventMask(int val) {
        this.val = val;
    }

    public int toInt() {
        return val;
    }

    /**
     * @return true if found any coincidence of bits
     */
    public boolean containsPartially(SettingListenerEventMask mask){
        return (val & mask.val) != 0;
    }

    public boolean containsPartially(int mask){
        return (val & mask) != 0;
    }

    /**
     * @return true if found complete coincidence of bits
     */
    public boolean containsComplete(SettingListenerEventMask mask){
        return (val & mask.val) == mask.val;
    }

    public boolean containsComplete(int mask){
        return (val & mask) == mask;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingListenerEventMask)) return false;
        return ((SettingListenerEventMask)obj).val == val;
    }

    /**
     * @return new mask which contains the specific mask
     */
    public SettingListenerEventMask append(SettingListenerEventMask mask){
        return build(this, mask);
    }

    /**
     * @return new mask which does not contain the specific mask
     */
    public SettingListenerEventMask detach(SettingListenerEventMask mask){
        return new SettingListenerEventMask(this.val & ~(mask.val));
    }

    /**
     * @return new mask which contains all the specific masks
     */
    public SettingListenerEventMask append(SettingListenerEventMask ...masks){
        return build(this, masks);
    }

    @Override
    public String toString() {
        return Integer.toString(val);
    }

    @Override
    public int hashCode() {
        return val;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SettingListenerEventMask(val);
    }

    // static

    public static SettingListenerEventMask build(SettingListenerEventMask mask0,
                                                 SettingListenerEventMask mask1){
        return new SettingListenerEventMask(mask0.val | mask1.val);
    }

    public static SettingListenerEventMask build(SettingListenerEventMask mask0,
                                                 SettingListenerEventMask ...masks){
        int res = mask0.val;
        for (final SettingListenerEventMask mask : masks) {
            res |= mask.val;
        }
        return new SettingListenerEventMask(res);
    }
}
