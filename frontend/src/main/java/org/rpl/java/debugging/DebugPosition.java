/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.backend.java.debugging;

public class DebugPosition {
    private final int line;
    private final String fileName;

    public DebugPosition(String fileName, int line) {
        this.fileName = fileName;
        this.line = line;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLine() {
        return line;
    }
}
