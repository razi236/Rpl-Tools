package org.rpl.frontend.typechecker;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.rpl.frontend.ast.*;
import org.rpl.frontend.typechecker.nullable.NullCheckerExtension;
public class Interface1Type extends ReferenceType {
    private final java.util.List<Type> supertypes;
    private final InterfaceDecl1 decl;

    public Interface1Type(InterfaceDecl1 decl) {
        this.decl = decl;
        this.supertypes = new java.util.ArrayList<>();
        for (InterfaceTypeUse i : decl.getExtendedInterfaceUses()) {
            supertypes.add(i.getType());
        }
    }

    public InterfaceDecl1 getDecl() {
        return decl;
    }
    @Override
    public boolean isInterface1Type() {
        return true;
    }

    @Override
    public boolean isDeploymentComponentType() {
        // KLUDGE: we need a proper subtyping check here -- but it's all
        // contained in RPLlang.rpl so we make do for now
        return getQualifiedName().equals("ABS.DC.DeploymentComponent")
            || getQualifiedName().equals("ABS.DC.DC")
            || getQualifiedName().equals("ABS.DC.DeploymentComponentForCloudProvider");
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Interface1Type))
            return false;
        Interface1Type t = (Interface1Type) o;
        return t.decl.equals(this.decl);
    }

    @Override
    public int hashCode() {
        return decl.hashCode();
    }

    @Override
    public boolean isAssignableTo(Type t, boolean considerSubtyping) {
        if (super.isAssignableTo(t))
            return true;

        if (considerSubtyping) {
            if (isAssignable(t, new HashSet<>()))
                return true;
        }
        return false;
    }

    private boolean isAssignable(Type t, Set<Type> visitedTypes) {
        if (visitedTypes.contains(this))
            return false;

        visitedTypes.add(this);
        if (super.isAssignableTo(t))
            return true;

        for (Type it : supertypes) {
            if (it.isInterfaceType()) { // maybe UnkownType
                if (((Interface1Type)it).isAssignable(t, visitedTypes))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAssignableTo(Type t) {
        return this.isAssignableTo(t, true);
    }

    @Override
    public String toString() {
        return decl.getName();
    }

    @Override
    public String getModuleName() {
        return decl.getModuleDecl().getName();
    }

    @Override
    public String getSimpleName() {
        return decl.getName();
    }

    @Override
    public MethodSig lookupMethod(String name) {
        return decl.lookupMethod(name);
    }

    @Override
    public MethodSig lookupMethod1(String name) {
        return decl.lookupMethod(name);
    }

    @Override
    public Type copy() {
        return new Interface1Type(decl);
    }

    @Override
    public Collection<MethodSig> getAllMethodSigs() {
        return decl.getAllMethodSigs();
    }

    @Override
    public Collection<MethodSig> getAllMethodSigs1() {
        return decl.getAllMethodSigs();
    }

    @Override
    public InterfaceTypeUse toUse() {
        return new InterfaceTypeUse(getQualifiedName(), NullCheckerExtension.getAnnotations(this));
    }

}
