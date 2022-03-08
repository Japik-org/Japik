package com.pro100kryto.server.dep;

import java.util.List;

public interface IDependency {
    DependencyLocationType getLocationType();
    String getId();
    DependencyType getDependencyType();
    DependencySide getDependencySide();
    List<Tenant> getTenantList();
    DependencyStatus getStatus();
    List<IDependency> getDependencyList();
    ClassLoader getClassLoader();

    void resolve() throws ResolveDependencyException;
    void destroy();
}
