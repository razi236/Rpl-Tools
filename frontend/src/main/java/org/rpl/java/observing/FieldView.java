/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.observing;

import org.rpl.backend.java.lib.types.ABSType;

public interface FieldView {
    public ClassView getClassView();
    public String getName();
    public ABSType getType();
}
