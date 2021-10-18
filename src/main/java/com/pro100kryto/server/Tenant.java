package com.pro100kryto.server;


public final class Tenant {
    private final String name;

    public Tenant(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Tenant { "+name+" }";
    }
}
