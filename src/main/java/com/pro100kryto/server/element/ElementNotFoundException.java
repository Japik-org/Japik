package com.pro100kryto.server.element;

import lombok.Getter;

@Getter
public class ElementNotFoundException extends Throwable {
    private final ElementType elementType;
    private final String elementSubtype;
    private final String elementName;

    public ElementNotFoundException(ElementType elementType, String elementName) {
        this.elementType = elementType;
        this.elementName = elementName;
        elementSubtype = "unknown";
    }

    public ElementNotFoundException(ElementType elementType, String elementSubtype, String elementName) {
        this.elementType = elementType;
        this.elementSubtype = elementSubtype;
        this.elementName = elementName;
    }

    @Override
    public String getMessage() {
        return "Element not found type='"+elementType+"' subtype='"+elementSubtype+"' name='"+elementName+"'";
    }
}
