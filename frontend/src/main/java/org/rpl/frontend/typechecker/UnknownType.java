/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.typechecker;

public final class UnknownType extends Type {
    public static final UnknownType INSTANCE = new UnknownType();

    private UnknownType() {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UnknownType;
    }

    public int hashCode() {
        return 42;
    }

    @Override
    public boolean isUnknownType() {
        return true;
    }

    @Override
    public boolean isAssignableTo(Type t) {
        return false;
    }

    public String toString() {
        return getSimpleName();
    }

    /**
     * Returns "<UNKOWN>"
     */
    @Override
    public String getSimpleName() {
        return "<UNKNOWN>";
    }

    @Override
    public Type copy() {
        return new UnknownType();
    }
}
