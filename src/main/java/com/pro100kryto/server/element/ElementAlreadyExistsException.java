package com.pro100kryto.server.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ElementAlreadyExistsException extends Exception{
    private final IElement element;
}
