/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.locationtypes;

import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.typechecker.TypeCheckerException;

public class LocationTypeCheckerException extends TypeCheckerException {

    public LocationTypeCheckerException(TypeError error) {
        super(error);
    }

}
