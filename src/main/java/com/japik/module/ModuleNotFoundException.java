package com.japik.module;

import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;
import lombok.Getter;

@Getter
public final class ModuleNotFoundException extends ElementNotFoundException {
    private final String serviceName;

    public ModuleNotFoundException(String elementSubtype, String elementName, String serviceName) {
        super(ElementType.Module, elementSubtype, elementName);
        this.serviceName = serviceName;
    }

    public ModuleNotFoundException(String elementName, String serviceName) {
        super(ElementType.Module, elementName);
        this.serviceName = serviceName;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "serviceName='"+serviceName+"'";
    }
}
