/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime;

import java.util.List;

import org.rpl.backend.java.lib.types.ABSRef;
import org.rpl.backend.java.lib.types.ABSValue;

public interface AsyncCall<T extends ABSRef> {

    Object execute();
    
    String methodName();
    
    List<ABSValue> getArgs();
    
    T getTarget();
    
    ABSObject getSource();

    Task<?> getSender();
    
}
