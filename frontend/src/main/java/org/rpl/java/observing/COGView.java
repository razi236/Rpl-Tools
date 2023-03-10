/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.observing;

public interface COGView {
    TaskSchedulerView getScheduler();

    void registerObjectCreationListener(ObjectCreationObserver listener);

    void registerObjectCreationListener(String className, ObjectCreationObserver e);

    int getID();
}
