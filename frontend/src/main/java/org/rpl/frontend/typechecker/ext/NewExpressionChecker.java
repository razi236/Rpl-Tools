/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.ext;

import org.rpl.common.CompilerUtils;
import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.SemanticError;
import org.rpl.frontend.analyser.AnnotationHelper;
import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.ast.ASTNode;
import org.rpl.frontend.ast.AssignStmt;
import org.rpl.frontend.ast.ExpressionStmt;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.NewExp;
import org.rpl.frontend.ast.PureExp;
import org.rpl.frontend.ast.Stmt;
import org.rpl.frontend.ast.VarDeclStmt;

/**
 * @author rudi
 *
 * Checks for type correctness of `new' expression annotations.
 *
 * - DC annotation must be of type ABS.DC.DeploymentComponent
 *
 * - DC annotation cannot be on `new local' expression
 *
 * - Deployment components cannot be created with `new local'
 */
public class NewExpressionChecker extends DefaultTypeSystemExtension {

    protected NewExpressionChecker(Model m) {
        super(m);
    }

    @Override
    public void checkExpressionStmt(ExpressionStmt expressionStmt) {
        checkDCCorrect(expressionStmt, AnnotationHelper.getAnnotationValueFromName(expressionStmt.getAnnotations(), "ABS.DC.DC"));
    }

    @Override
    public void checkAssignStmt(AssignStmt s) {
        checkDCCorrect(s, AnnotationHelper.getAnnotationValueFromName(s.getAnnotations(), "ABS.DC.DC"));
    }

    @Override
    public void checkVarDeclStmt(VarDeclStmt varDeclStmt) {
        checkDCCorrect(varDeclStmt, AnnotationHelper.getAnnotationValueFromName(varDeclStmt.getAnnotations(), "ABS.DC.DC"));
    }

    private void checkDCCorrect(ASTNode<?> n, PureExp dc) {
        if (dc == null) return;
        dc.typeCheck(errors);
        if (dc.getType().isUnknownType()
            || !dc.getType().isDeploymentComponentType()) {
            errors.add(new TypeError(n, ErrorMessage.WRONG_DEPLOYMENT_COMPONENT, dc.getType().getQualifiedName()));
        }
    }

    @Override
    public void checkNewExp(NewExp e) {
        if (e.hasLocal()) {
            if (e.getType().isDeploymentComponentType()) {
                // Don't create a deployment component with "new local"
                errors.add(new SemanticError(e, ErrorMessage.DEPLOYMENT_COMPONENT_NOT_COG, "dummy string to keep constructor happy"));
            }
            Stmt stmt = CompilerUtils.findStmtForExpression(e);
            if (stmt != null) { // should always be true
                if (AnnotationHelper.getAnnotationValueFromName(stmt.getAnnotations(), "ABS.DC.DC") != null) {
                    errors.add(new SemanticError(e, ErrorMessage.DEPLOYMENT_COMPONENT_IGNORED, "dummy string to keep constructor happy"));
                }
            }
        }
    }
}
