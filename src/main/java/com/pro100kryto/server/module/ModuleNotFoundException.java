package com.pro100kryto.server.module;

import com.pro100kryto.server.element.ElementNotFoundException;
import com.pro100kryto.server.element.ElementType;
import lombok.Getter;

@Getter
public final class ModuleNotFoundException extends ElementNotFoundException {
    private final String serviceName;

    public ModuleNotFoundException(ElementType elementType, String elementSubtype, String elementName, String serviceName) {
        super(elementType, elementSubtype, elementName);
        this.serviceName = serviceName;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "serviceName='"+serviceName+"'";
    }
}
