package com.pro100kryto.server.extension;


import com.pro100kryto.server.element.IElement;

public interface IExtension <EC extends IExtensionConnection> extends IElement {
    EC createExtensionConnection();
}
