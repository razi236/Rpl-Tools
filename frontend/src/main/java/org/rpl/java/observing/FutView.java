/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.observing;

import org.rpl.backend.java.lib.types.ABSValue;

public interface FutView {
    TaskView getResolvingTask();

    boolean isResolved();

    ABSValue getValue();

    void registerFutObserver(FutObserver obs);

    int getID();
}
