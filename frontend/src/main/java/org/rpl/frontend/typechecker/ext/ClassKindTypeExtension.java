/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker.ext;

import java.util.List;

import org.rpl.frontend.analyser.AnnotationHelper;
import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.TypeError;
import org.rpl.frontend.ast.Annotation;
import org.rpl.frontend.ast.ClassDecl;
import org.rpl.frontend.ast.DataConstructorExp;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.NewExp;
import org.rpl.frontend.typechecker.KindedName;
import org.rpl.frontend.typechecker.KindedName.Kind;

public class ClassKindTypeExtension extends DefaultTypeSystemExtension {
    
    public ClassKindTypeExtension(Model m) {
        super(m);
    }

    @Override
    public void checkNewExp(NewExp e) {
        ClassDecl d = (ClassDecl) e.lookup(new KindedName(Kind.CLASS,e.getClassName()));
        List<Annotation> anns = AnnotationHelper.getAnnotationsOfType(d.getAnnotations(), "ABS.StdLib.ClassKindAnnotation");
            
        if (!anns.isEmpty()) {
            String name = ((DataConstructorExp) anns.get(0).getValue()).getDecl().getName();
            if (e.hasLocal()) {
                if (name.equals("COG")) {
                    errors.add(new TypeError(e,ErrorMessage.CLASSKIND_PLAIN,d.getName()));
                }
            } else {
                if (!name.equals("COG")) {
                    errors.add(new TypeError(e,ErrorMessage.CLASSKIND_COG,d.getName()));
                }
            }
        }
    }

}
