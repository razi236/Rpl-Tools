/**
 * Copyright (c) 2016, The Envisage Project. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.ext;

import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.AnnotationHelper;
import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.PureExp;
import org.rpl.frontend.ast.Stmt;

public class CostAnnotationChecker extends DefaultTypeSystemExtension {

    protected CostAnnotationChecker(Model m) {
        super(m);
    }

    @Override
    public void checkStmt(Stmt s) {
        PureExp cost = AnnotationHelper.getAnnotationValueFromName(s.getAnnotations(), "ABS.DC.Cost");
        if (cost == null) return;
        cost.typeCheck(errors);
        if (!cost.getType().isNumericType()) {
            errors.add(new TypeError(s, ErrorMessage.WRONG_COST_ANNOTATION_TYPE,
                                     cost.getType().getQualifiedName()));
        }
    }
}
