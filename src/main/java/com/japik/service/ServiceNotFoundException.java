package com.japik.service;

import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;
import lombok.Getter;

@Getter
public final class ServiceNotFoundException extends ElementNotFoundException {

    public ServiceNotFoundException(String elementSubtype, String elementName) {
        super(ElementType.Service, elementSubtype, elementName);
    }

    public ServiceNotFoundException(String elementName) {
        super(ElementType.Service, elementName);
    }
}
