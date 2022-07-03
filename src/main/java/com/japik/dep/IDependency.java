package com.japik.dep;

import java.io.IOException;
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

    void resolveAll() throws ResolveDependencyException;
    void resolveClassLoaders() throws Throwable; // 1
    void resolveClasses() throws ReflectiveOperationException, LinkageError, IOException; // 2

    void destroy();
}
