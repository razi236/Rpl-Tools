/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime;

import org.rpl.backend.java.lib.runtime.metaABS.Method;
import org.rpl.backend.java.lib.types.ABSValue;

public abstract class ABSClosure extends ABSDynamicObject {

    public ABSClosure() {
        super(Method.singleton());
    }

    // Run the method; to be implemented by subclasses
    public abstract ABSValue exec(ABSDynamicObject t, ABSValue... params);

}
