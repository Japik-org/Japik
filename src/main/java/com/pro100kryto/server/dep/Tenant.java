package com.pro100kryto.server.dep;


import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class Tenant {
    @Getter
    @NonNull
    private final String name;

    public Tenant(@NonNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Tenant: "+name+"";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        return name.equals(((Tenant)obj).name);
    }
}
