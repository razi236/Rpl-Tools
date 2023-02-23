package org.rpl.frontend.typechecker.nullable;

import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.typechecker.TypeCheckerException;

public class NullCheckerException extends TypeCheckerException {
    public NullCheckerException(TypeError error) {
        super(error);
    }
}
