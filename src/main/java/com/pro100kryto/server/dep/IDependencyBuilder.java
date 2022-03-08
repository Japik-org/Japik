package com.pro100kryto.server.dep;

public interface IDependencyBuilder {
    String getDependencyId();
    DependencyLocationType getLocationType();
    DependencyType getDependencyType();
    DependencySide getDependencySide();
    Tenant getTenant();

    boolean isInit();
    void init() throws Throwable;

    IDependency build(DependencyLord dependencyLord) throws Throwable;
}
