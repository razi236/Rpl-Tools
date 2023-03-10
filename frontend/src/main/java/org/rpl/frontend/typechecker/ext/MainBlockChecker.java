/**
 * Copyright (c) 2016, The Envisage Project. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */

package org.rpl.frontend.typechecker.ext;

import org.rpl.frontend.analyser.ErrorMessage;
import org.rpl.frontend.analyser.SemanticWarning;
import org.rpl.frontend.ast.Block;
import org.rpl.frontend.ast.CompilationUnit;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.ast.ModuleDecl;

public class MainBlockChecker extends DefaultTypeSystemExtension {
    protected MainBlockChecker(Model m) {
        super(m);
    }

    @Override
    public void checkModel(Model model) {
        int nMainBlocks = 0;
        for (CompilationUnit u : model.getCompilationUnits()) {
            for (ModuleDecl m : u.getModuleDecls()) {
                if (m.hasBlock()) {
                    nMainBlocks = nMainBlocks + 1;
                }
            }
        }
        if (nMainBlocks == 0) {
            CompilationUnit c = model.getCompilationUnit(0);
            errors.add(new SemanticWarning(c, ErrorMessage.MAIN_BLOCK_NOT_FOUND, "dummy string to keep constructor happy"));
        } else if (nMainBlocks > 1) {
            Block b = model.getMainBlock();
            String moduleName = ((ModuleDecl)(b.getParent().getParent())).getName();
            for (CompilationUnit u : model.getCompilationUnits()) {
                for (ModuleDecl m : u.getModuleDecls()) {
                    if (m.hasBlock() && m.getBlock() != b) {
                        errors.add(new SemanticWarning(m.getBlock(), ErrorMessage.MAIN_BLOCK_AMBIGUOUS, moduleName));
                    }
                }
            }
        }
    }
}

