package com.japik.extension;


import com.japik.element.IElement;

public interface IExtension <EC extends IExtensionConnection> extends IElement {
    EC createExtensionConnection();
}
