/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime;

import org.rpl.backend.java.lib.types.ABSValue;

public class ABSField {

    // to be overridden
    public ABSValue init(ABSDynamicObject t) {
        return null;
    }
}
