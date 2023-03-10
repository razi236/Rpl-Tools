/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.expr;

import java.util.ArrayList;

import org.rpl.backend.java.lib.types.ABSValue;

public class PatternBinding {
    ArrayList<ABSValue> binding = new ArrayList<>();

    public void addBinding(ABSValue dt) {
        binding.add(dt);
    }

    public ABSValue getBinding(int i) {
        return binding.get(i);
    }
}
