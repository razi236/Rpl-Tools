/**
 * Copyright (c) 2014, Rudolf Schlatte. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.antlr.parser;

import org.rpl.frontend.parser.ParserError;
import org.rpl.frontend.parser.SyntaxError;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class SyntaxErrorCollector extends BaseErrorListener {

    private java.io.File filename = null;

    public SyntaxErrorCollector(java.io.File file) {
        this.filename = file;
    }

    public java.util.List<ParserError> parserErrors
        = new java.util.ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException e)
    {
        SyntaxError err = new SyntaxError(msg, line, charPositionInLine + 1);
        err.setFile(filename);
        parserErrors.add(err);
        // super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
    }
}
