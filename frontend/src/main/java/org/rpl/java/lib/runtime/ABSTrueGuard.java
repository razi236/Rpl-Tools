/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime;

public class ABSTrueGuard extends ABSGuard {

    @Override
    public boolean isTrue() {
        return true;
    }

    @Override
    public String toString() {
        return "TrueGuard";
    }

    @Override
    public String toABSString() {
        return "True";
    }
}
