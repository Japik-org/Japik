package com.pro100kryto.server.service;

import com.pro100kryto.server.element.ElementNotFoundException;
import com.pro100kryto.server.element.ElementType;
import lombok.Getter;

@Getter
public final class ServiceNotFoundException extends ElementNotFoundException {

    public ServiceNotFoundException(ElementType elementType, String elementSubtype, String elementName) {
        super(elementType, elementSubtype, elementName);
    }
}
