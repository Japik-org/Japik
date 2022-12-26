package com.japik.extension;

import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;

public final class ExtensionNotFoundException extends ElementNotFoundException {

    public ExtensionNotFoundException(String elementName) {
        super(ElementType.Extension, elementName);
    }

    public ExtensionNotFoundException(String elementSubtype, String elementName) {
        super(ElementType.Extension, elementSubtype, elementName);
    }

}
