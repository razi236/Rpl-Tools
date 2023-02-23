/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.observing;

import org.rpl.backend.java.lib.runtime.ABSException;

/**
 * An interface to observer global system behavior
 * 
 * @author Jan Schäfer
 *
 */
public interface SystemObserver {
    void systemStarted();

    void newCOGCreated(COGView cog, ObjectView initialObject);
    
    void systemError(ABSException e);
    
    void systemFinished();
}
