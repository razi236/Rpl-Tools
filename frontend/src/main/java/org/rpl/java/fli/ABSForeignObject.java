/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.fli;

import org.rpl.backend.java.lib.types.ABSBool;
import org.rpl.backend.java.lib.types.ABSRef;
import org.rpl.backend.java.lib.types.ABSValue;

public abstract class ABSForeignObject implements ABSRef {

    @Override
    public final ABSBool eq(ABSValue o) {
        return ABSBool.fromBoolean(this == o);
    }

    @Override
    public final ABSBool notEq(ABSValue o) {
        return eq(o).negate();
    }

    @Override
    public final boolean isDataType() {
        return false;
    }

    @Override
    public final boolean isReference() {
        return true;
    }
    
}
