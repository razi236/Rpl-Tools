/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.expr;

import org.rpl.backend.java.lib.types.ABSValue;

public class PatternValue extends Pattern {

    private final ABSValue value;

    public PatternValue(ABSValue value) {
        this.value = value;
    }

    @Override
    public boolean match(ABSValue dt, PatternBinding binding) {
        return value.eq(dt).toBoolean();
    }

}
