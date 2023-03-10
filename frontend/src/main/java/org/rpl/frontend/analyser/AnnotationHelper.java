/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.analyser;

import java.util.ArrayList;

import org.rpl.frontend.ast.Annotation;
import org.rpl.frontend.ast.DataConstructorExp;
import org.rpl.frontend.ast.List;
import org.rpl.frontend.ast.PureExp;
import org.rpl.frontend.ast.TypedAnnotation;


public final class AnnotationHelper {
    private AnnotationHelper() {};

    public static java.util.List<Annotation> getAnnotationsOfType(List<Annotation> annos, String qualifiedName) {
        ArrayList<Annotation> res = new ArrayList<>();
        for (Annotation a : annos) {
            if (a.getType().getQualifiedName().equals(qualifiedName)) {
                DataConstructorExp de = (DataConstructorExp) a.getValue();
                res.add(a);
            }
        }
        return res;
    }

        /**
     * Get the value of an annotation.  Will return the value of the first
     * annotation with the given simple name.
     *
     * @param annotationName The simple name (without module prefix) of the
     * annotation
     * @return a <code>PureExp</code> value or null
     */
    public static PureExp getAnnotationValueFromSimpleName(List<Annotation> annotations, String annotationName) {
        for (Annotation a : annotations) {
            if (a instanceof TypedAnnotation) {
                TypedAnnotation ta = (TypedAnnotation)a;
                if (ta.getTypeIdUse().getName().equals(annotationName))
                    return ta.getValue();
            }
        }
        return null;
    }

    /**
     * Get the value of an annotation.  Returns the value of the first
     * annotation with the given qualified (module-prefixed) name.
     *
     * @param annotations The list of annotations
     * @param qualifiedAnnotationName The qualified name of the annotation
     * @return a <code>PureExp</code> value or null
     */
    public static PureExp getAnnotationValueFromName(List<Annotation> annotations, String qualifiedAnnotationName) {
        for (Annotation a : annotations) {
            if (a instanceof TypedAnnotation) {
                TypedAnnotation ta = (TypedAnnotation)a;
                if (ta.getTypeIdUse().getDecl().getQualifiedName().equals(qualifiedAnnotationName))
                    return ta.getValue();
            }
        }
        return null;
    }


}
