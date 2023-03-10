/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.debugging;

import org.rpl.backend.java.observing.TaskView;

public interface Debugger {
    public void nextStep(TaskView taskView, String fileName, int line);
}
