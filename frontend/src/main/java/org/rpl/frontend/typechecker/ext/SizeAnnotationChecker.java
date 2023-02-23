/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.ext;

import org.rpl.frontend.analyser.AnnotationHelper;
import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.ast.ASTNode;
import org.rpl.frontend.ast.AssignStmt;
import org.rpl.frontend.ast.ExpressionStmt;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.PureExp;
import org.rpl.frontend.ast.ReturnStmt;
import org.rpl.frontend.ast.VarDeclStmt;

public class SizeAnnotationChecker extends DefaultTypeSystemExtension {

    protected SizeAnnotationChecker(Model m) {
        super(m);
    }

    @Override
    public void checkExpressionStmt(ExpressionStmt e) {
        checkSizeAnnotationCorrect(e, AnnotationHelper.getAnnotationValueFromName(e.getAnnotations(), "ABS.DC.DataSize"));
    }

    @Override
    public void checkAssignStmt(AssignStmt s) {
        checkSizeAnnotationCorrect(s, AnnotationHelper.getAnnotationValueFromName(s.getAnnotations(), "ABS.DC.DataSize"));
    }

    @Override
    public void checkVarDeclStmt(VarDeclStmt v) {
        checkSizeAnnotationCorrect(v, AnnotationHelper.getAnnotationValueFromName(v.getAnnotations(), "ABS.DC.DataSize"));
    }

    @Override
    public void checkReturnStmt(ReturnStmt s) {
        checkSizeAnnotationCorrect(s, AnnotationHelper.getAnnotationValueFromName(s.getAnnotations(), "ABS.DC.DataSize"));
    }

    private void checkSizeAnnotationCorrect(ASTNode<?> n, PureExp size) {
        if (size == null) return;
        size.typeCheck(errors);
        if (!size.getType().isNumericType()) {
            errors.add(new TypeError(n, ErrorMessage.WRONG_SIZE_ANNOTATION_TYPE,
                                     size.getType().getQualifiedName()));
        }
    }
}
