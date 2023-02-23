/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.ext;

import java.util.List;

import org.rpl.frontend.analyser.AnnotationHelper;
import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.ast.ASTNode;
import org.rpl.frontend.ast.Annotation;
import org.rpl.frontend.ast.AssignStmt;
import org.rpl.frontend.ast.AwaitAsyncCall;
import org.rpl.frontend.ast.AwaitStmt;
import org.rpl.frontend.ast.Call;
import org.rpl.frontend.ast.FieldDecl;
import org.rpl.frontend.ast.GetExp;
import org.rpl.frontend.ast.MethodImpl;
import org.rpl.frontend.ast.MethodSig;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.SuspendStmt;

public class AtomicityChecker extends DefaultTypeSystemExtension {

    protected AtomicityChecker(Model m) {
        super(m);
    }

    @Override
    public void checkOverride(MethodSig impl, MethodSig overridden) {
        if (!impl.isAtomic() && overridden.isAtomic()) {
            errors.add(new TypeError(impl, ErrorMessage.ATOMIC_METHOD_WRONG_OVERRIDE,impl.getName(),impl.getName(),overridden.getContextDecl().getName()));
        }
        if (!impl.isReadonly() && overridden.isReadonly()) {
            errors.add(new TypeError(impl, ErrorMessage.READONLY_METHOD_WRONG_OVERRIDE,impl.getName(),impl.getName(),overridden.getContextDecl().getName()));
        }
    }

    @Override
    public void checkAwaitStmt(AwaitStmt s) {
        ensureNonAtomic(s,s.getContextMethod(),"an await statement");
    }

    @Override
    public void checkSuspendStmt(SuspendStmt s) {
        ensureNonAtomic(s,s.getContextMethod(),"a suspend statement");
    }

    @Override
    public void checkGetExp(GetExp e) {
        ensureNonAtomic(e,e.getContextMethod(),"a blocking get expression");
    }

    @Override
    public void checkMethodCall(Call call) {
        if (!call.isAsync()) {
            MethodSig sig = call.getMethodSig();
            if (!isAtomic(sig.getReturnType().getAnnotations())) {
                ensureNonAtomic(call, call.getContextMethod(), "a synchronous call to non-atomic method " + sig.getName());
            }
        } else if (call instanceof AwaitAsyncCall) {
            // This is for the case when AwaitAsyncCall rewriting is turned
            // off (e.g., in the Maude backend); otherwise, this case is
            // caught by the `await' and `get' changes elsewhere in this file.
            ensureNonAtomic(call, call.getContextMethod(), "an await expression");
        }
    }

    private void ensureNonAtomic(ASTNode<?> n, MethodImpl impl, String descr) {
        if (impl == null)
            return;
        MethodSig sig = impl.getMethodSig();
        if (sig.isAtomic()) {
            errors.add(new TypeError(n, ErrorMessage.ATOMIC_METHOD_CONTAINS_ILLEGAL_CODE,descr,sig.getName()));
        }
    }

    public static boolean isAtomic(org.rpl.frontend.ast.List<Annotation> list) {
        List<Annotation> anns = AnnotationHelper.getAnnotationsOfType(list, "ABS.StdLib.AtomicityAnnotation");
        return !anns.isEmpty();
    }



    @Override
    public void checkAssignStmt(AssignStmt s) {
        MethodImpl m = s.getContextMethod();
        if (m != null && m.isReadonly()) {
            if (s.getVar().getDecl() instanceof FieldDecl) {
                errors.add(new TypeError(s, ErrorMessage.READONLY_METHOD_HAS_FIELD_ASSIGNMENT, s.getVar().getName()));
            }
        }
    }

    public static boolean isReadonly(org.rpl.frontend.ast.List<Annotation> list) {
        List<Annotation> anns = AnnotationHelper.getAnnotationsOfType(list, "ABS.StdLib.ReadonlyAnnotation");
        return !anns.isEmpty();
    }

}
