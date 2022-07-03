package com.japik.service;

import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;
import lombok.Getter;

@Getter
public final class ServiceNotFoundException extends ElementNotFoundException {

    public ServiceNotFoundException(ElementType elementType, String elementSubtype, String elementName) {
        super(elementType, elementSubtype, elementName);
    }
}
