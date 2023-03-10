/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.lib.runtime;

public interface AsyncCallRTAttributes {
    long getDeadline();
    long getCost();
    boolean isCritical();
}
