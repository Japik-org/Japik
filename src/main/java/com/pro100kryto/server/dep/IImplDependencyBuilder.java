package com.pro100kryto.server.dep;

import org.jetbrains.annotations.Nullable;

public interface IImplDependencyBuilder extends IDependencyBuilder {
    @Nullable
    ISharedDependencyBuilder getSharedDependencyBuilder();
}
