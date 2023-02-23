package org.rpl.frontend.typechecker.nullable;

import org.rpl.frontend.ast.Annotation;
import org.rpl.frontend.ast.DataConstructorExp;

/**
 * The nullable types of expressions
 */
public enum NullableType {
    /**
     * The expression is always null
     */
    Null,
    /**
     * The expression or declaration can never be null
     */
    Nonnull,
    /**
     * The expression or declaration may be null
     */
    Nullable;

    public static final NullableType[] USER_TYPES = {Nonnull, Nullable};

    /**
     * Whether this is assignable to `n`
     * @param n - The type to check against
     * @return - True iff this is assignable to `n`
     */
    public boolean assignableTo(NullableType n) {
        return NullableType.assignable(n, this);
    }

    /**
     * Gets the most common nullable tyoe between this and `other`
     * @param other - The other type
     * @return - The most common type
     */
    public NullableType getMostCommon(NullableType other) {
        if (this == Nullable || other == Nullable) {
            return Nullable;
        }

        if (this == Nonnull) {
            if (other == Nonnull) {
                return Nonnull;
            }
            return Nullable;
        }

        if (this == Null) {
            if (other == Null) {
                return Null;
            }
        }
        return Nullable;
    }

    /**
     * @param lhs - The left hand type
     * @param rhs - The right hand type
     * @return - Whether the assignment lhs = rhs would be correct
     */
    public static boolean assignable(NullableType lhs, NullableType rhs) {
        if (lhs == NullableType.Nonnull) {
            return rhs == NullableType.Nonnull;
        }
        return true;
    }

    /**
     * Tries to compute the nullable type from a string
     * @param name - The name of the type
     * @return - The converted type
     */
    public static NullableType fromName(String name) {
        if (name.equals("Nonnull")) {
            return NullableType.Nonnull;
        }
        return NullableType.Nullable;
    }

    /**
     * @return - Whether `this` is NonNull
     */
    public boolean isNonnull() {
        return this == Nonnull;
    }

    /**
     * @return - Whether `this` is Null
     */
    public boolean isNull() {
        return this == Null;
    }

    /**
     * @return - Whether `this` is Nullable
     */
    public boolean isNullable() {
        return this == Nullable;
    }

    /**
     *
     * @return - The annotation for this type
     */
    public Annotation toAnnotation() {
        if (isNull()) {
            throw new IllegalArgumentException("Cannot turn Null into annotation.");
        }
        return new Annotation(new DataConstructorExp(toString(), new org.rpl.frontend.ast.List<>()));
    }
}
