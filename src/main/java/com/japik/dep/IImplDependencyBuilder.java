package com.japik.dep;

import org.jetbrains.annotations.Nullable;

public interface IImplDependencyBuilder extends IDependencyBuilder {
    @Nullable
    ISharedDependencyBuilder getSharedDependencyBuilder();
}
