package org.rpl.frontend.typechecker.locationtypes;

import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.typechecker.TypeCheckerException;

public class LocationTypeInferException extends TypeCheckerException {
    public LocationTypeInferException(TypeError error) {
        super(error);
    }
}
