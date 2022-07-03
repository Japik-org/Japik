package com.japik.module;

import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;
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
