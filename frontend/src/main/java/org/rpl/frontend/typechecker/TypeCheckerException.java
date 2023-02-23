/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker;

import org.rpl.frontend.analyser.TypeError;

public class TypeCheckerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final TypeError typeError;

    public TypeCheckerException(TypeError error) {
        super(error.getHelpMessage());
        this.typeError = error;
    }

    public TypeError getTypeError() {
        return typeError;
    }

}
