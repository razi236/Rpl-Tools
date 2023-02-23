/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.observing;

import java.util.List;

import org.rpl.backend.java.lib.types.ABSValue;

public interface ObjectView {
    COGView getCOG();

    ClassView getClassView();

    String getClassName();

    ABSValue getFieldValue(String fieldName) throws NoSuchFieldException;

    void registerObjectObserver(ObjectObserver l);

    List<String> getFieldNames();

    long getID();
}
