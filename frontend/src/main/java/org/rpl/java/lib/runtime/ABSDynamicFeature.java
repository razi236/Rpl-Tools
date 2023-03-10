/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime;

import org.rpl.backend.java.lib.runtime.metaABS.Feature;

public class ABSDynamicFeature extends ABSDynamicObject {

    public ABSDynamicFeature() {
        super(Feature.singleton());
    }

    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}
