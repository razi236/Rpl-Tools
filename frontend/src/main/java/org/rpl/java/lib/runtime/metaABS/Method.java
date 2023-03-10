/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime.metaABS;

import org.rpl.backend.java.lib.runtime.ABSClosure;
import org.rpl.backend.java.lib.runtime.ABSDynamicClass;
import org.rpl.backend.java.lib.runtime.ABSDynamicObject;
import org.rpl.backend.java.lib.types.ABSUnit;
import org.rpl.backend.java.lib.types.ABSValue;

public class Method {
    private static ABSDynamicClass thisClass;

    /* 
     * Create singleton object
     */
    public static ABSDynamicClass singleton() {
        if (thisClass == null) {
            thisClass = new ABSDynamicClass();
            setupAPI();
        }
        return thisClass;
    }
    
    /*
     * Define the methods of this class
     */
    public static void setupAPI() {
        thisClass.setName("Method");
        
        // FIXME This does not work currently, because RPLlang.rpl needs to declare
        // * the return type
        // * the number and types of arguments
        thisClass.addMethod(/*ABSValue*/ "exec", new ABSClosure() {
            @Override
            public ABSValue exec(ABSDynamicObject t, ABSValue... params) {
                ABSClosure method = (ABSClosure)t;

                // FIXME
                // params[0] is the receiver object, 
                // params[1] is an ABSList with the actual arguments
                ABSValue res = method.exec((ABSDynamicObject)params[0], params[1]);
                
                // FIXME return the result...
                return ABSUnit.UNIT;
            }
        });

    }
}
