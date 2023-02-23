/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime.metaABS;

import org.rpl.backend.java.lib.runtime.ABSClosure;
import org.rpl.backend.java.lib.runtime.ABSDynamicClass;
import org.rpl.backend.java.lib.runtime.ABSDynamicDelta;
import org.rpl.backend.java.lib.runtime.ABSDynamicObject;
import org.rpl.backend.java.lib.types.ABSString;
import org.rpl.backend.java.lib.types.ABSUnit;
import org.rpl.backend.java.lib.types.ABSValue;

public class Delta {
    private static ABSDynamicClass thisClass;

    /* 
     * Create singleton object
     */
    public static ABSDynamicClass singleton() {
        if (thisClass == null) {
            thisClass = new ABSDynamicClass();
            setupMetaAPI();
        }
        return thisClass;
    }

    /*
     * Define the methods of this class
     */
    public static void setupMetaAPI() {
        thisClass.setName("Delta");

        thisClass.addMethod(/*ABSString*/ "getName", new ABSClosure() {
            @Override
            public ABSString exec(ABSDynamicObject t, ABSValue... params) {
                ABSDynamicDelta delta = (ABSDynamicDelta)t;
                return ABSString.fromString(delta.getName());
            }
        });

        thisClass.addMethod(/*ABSUnit*/ "apply", new ABSClosure() {
            @Override
            public ABSValue exec(ABSDynamicObject t, ABSValue... params) {
                ABSDynamicDelta delta = (ABSDynamicDelta)t;
                delta.apply();
                return ABSUnit.UNIT;
            }
        });
        
    }
}
