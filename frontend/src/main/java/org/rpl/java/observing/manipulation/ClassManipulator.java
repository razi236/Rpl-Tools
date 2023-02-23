/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.observing.manipulation;

import org.rpl.backend.java.lib.runtime.ABSClosure;
import org.rpl.backend.java.observing.ClassView;

public interface ClassManipulator extends ClassView {
    void addField(String name, String type, ABSClosure init);
}
