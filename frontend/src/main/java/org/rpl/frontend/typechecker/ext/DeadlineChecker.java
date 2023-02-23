/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.ext;

import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.AnnotationHelper;
import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.ast.ASTNode;
import org.rpl.frontend.ast.AssignStmt;
import org.rpl.frontend.ast.ExpressionStmt;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.PureExp;
import org.rpl.frontend.ast.VarDeclStmt;

public class DeadlineChecker extends DefaultTypeSystemExtension {

    protected DeadlineChecker(Model m) {
        super(m);
    }

    @Override
    public void checkExpressionStmt(ExpressionStmt e) {
        checkDeadlineCorrect(e, AnnotationHelper.getAnnotationValueFromName(e.getAnnotations(), "ABS.StdLib.Deadline"));
    }

    @Override
    public void checkAssignStmt(AssignStmt s) {
        checkDeadlineCorrect(s, AnnotationHelper.getAnnotationValueFromName(s.getAnnotations(), "ABS.StdLib.Deadline"));
    }

    @Override
    public void checkVarDeclStmt(VarDeclStmt v) {
        checkDeadlineCorrect(v, AnnotationHelper.getAnnotationValueFromName(v.getAnnotations(), "ABS.StdLib.Deadline"));
    }

    private void checkDeadlineCorrect(ASTNode<?> n, PureExp deadline) {
        if (deadline == null) return;
        deadline.typeCheck(errors);
        if (deadline.getType().isUnknownType()
            || !deadline.getType().getQualifiedName().equals("ABS.StdLib.Duration")) {
            errors.add(new TypeError(n, ErrorMessage.WRONG_DEADLINE_TYPE,
                                     deadline.getType().getQualifiedName()));
        }
    }
}
