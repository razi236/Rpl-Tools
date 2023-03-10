/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.analyser;

import org.rpl.frontend.ast.ASTNode;
import org.rpl.frontend.ast.Name;
import org.rpl.frontend.typechecker.Type;

public class TypeError extends SemanticError {

    public TypeError(ASTNode<?> node, ErrorMessage msg, String... args) {
        super(node, msg, args);
    }

    public TypeError(ASTNode<?> node, ErrorMessage msg, Name... args) {
        super(node, msg, toString(args));
    }

    public TypeError(ASTNode<?> node, ErrorMessage msg, Type... args) {
        super(node, msg, toString(args));
    }

    public TypeError(ASTNode<?> node, ErrorMessage msg, Integer... args) {
        super(node, msg, toString(args));
    }

    private static String[] toString(Name[] args) {
        String[] res = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            res[i] = args[i].getString();
        }
        return res;
    }

    private static String[] toString(Type[] args) {
        String[] res = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            res[i] = args[i].toString();
        }
        return res;
    }

    private static String[] toString(Integer[] args) {
        String[] res = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            res[i] = args[i].toString();
        }
        return res;
    }

}
