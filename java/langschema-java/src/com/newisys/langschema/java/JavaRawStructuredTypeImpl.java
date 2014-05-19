/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.langschema.java;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.Namespace;

/**
 * Base implementation for raw Java types (as opposed to parameterized types).
 * 
 * @author Trevor Robinson
 */
public abstract class JavaRawStructuredTypeImpl<M extends JavaStructuredTypeMember>
    extends JavaStructuredTypeImpl<M>
    implements JavaRawStructuredType<M>
{
    protected final JavaName name;
    private JavaPackage pkg;
    private JavaVisibility visibility;
    private final Set<JavaTypeModifier> modifiers;
    protected final List<JavaTypeVariable> typeVariables;

    // TODO: weakly reference cached parameterizations
    private transient Map<Map<JavaTypeVariable, JavaReferenceType>, JavaParameterizedType<M>> paramCache;

    protected JavaRawStructuredTypeImpl(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, outerType);
        final Namespace ns = outerType != null ? outerType : pkg;
        this.name = new JavaName(id, JavaNameKind.TYPE, ns);
        this.pkg = pkg;
        this.visibility = JavaVisibility.DEFAULT;
        this.modifiers = new HashSet<JavaTypeModifier>();
        this.typeVariables = new LinkedList<JavaTypeVariable>();
    }

    final void copyFrom(JavaRawStructuredTypeImpl<M> other)
    {
        super.copyFrom(other);
        visibility = other.visibility;
        modifiers.addAll(other.modifiers);
        typeVariables.addAll(other.typeVariables);
    }

    public final JavaName getName()
    {
        return name;
    }

    public final JavaPackage getPackage()
    {
        return pkg;
    }

    public final void setPackage(JavaPackage pkg)
    {
        if (outerType != null && outerType.getPackage() != pkg)
        {
            throw new IllegalStateException(
                "Cannot set package of nested type '" + name.getIdentifier()
                    + "' to '" + (pkg != null ? pkg.getName() : "(default)")
                    + "', because it is different than package of outer type '"
                    + outerType.getName() + "'");
        }
        this.pkg = pkg;
        name.setNamespace(pkg);
    }

    public final void setStructuredType(JavaStructuredType< ? > outerType)
    {
        if (outerType != null)
        {
            final JavaPackage outerPkg = outerType.getPackage();
            if (pkg != null && pkg != outerPkg)
            {
                throw new IllegalStateException(
                    "Cannot set outer type of type '" + name + "' to '"
                        + outerType.getName()
                        + "', because it is in a different package");
            }
            this.pkg = outerPkg;
            name.setNamespace(outerType);
        }
        super.setStructuredType(outerType);
    }

    public final JavaVisibility getVisibility()
    {
        return visibility;
    }

    public final void setVisibility(JavaVisibility visibility)
    {
        this.visibility = visibility;
    }

    public final boolean isAccessible(JavaStructuredType< ? > fromType)
    {
        return JavaStructuredTypeImpl.isAccessible(this, fromType);
    }

    public final Set<JavaTypeModifier> getModifiers()
    {
        return modifiers;
    }

    public final void addModifier(JavaTypeModifier modifier)
    {
        modifiers.add(modifier);
    }

    public final JavaRawStructuredType<M> getRawType()
    {
        return this;
    }

    public final List<JavaTypeVariable> getTypeVariables()
    {
        return typeVariables;
    }

    public boolean hasTypeVariables()
    {
        return super.hasTypeVariables() || !typeVariables.isEmpty();
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        throw new UnsupportedOperationException(getClass().getName()
            + " cannot have type variables");
    }

    public boolean isSubtype(JavaType type)
    {
        return super.isSubtype(type)
            || (type instanceof JavaParameterizedType && ((JavaParameterizedType< ? >) type)
                .getRawType() == this);
    }

    private JavaParameterizedType<M> lookupParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return paramCache != null ? paramCache.get(typeVarMap) : null;
    }

    private void cacheParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        JavaParameterizedType<M> type)
    {
        if (paramCache == null)
        {
            paramCache = new HashMap<Map<JavaTypeVariable, JavaReferenceType>, JavaParameterizedType<M>>();
        }
        paramCache.put(typeVarMap, type);
    }

    protected JavaParameterizedType<M> createParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        throw new UnsupportedOperationException(getClass().getName()
            + " cannot be parameterized");
    }

    protected void resolveParameterization(
        JavaParameterizedType<M> paramType,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        throw new UnsupportedOperationException(getClass().getName()
            + " cannot be parameterized");
    }

    public JavaParameterizedType<M> parameterize(JavaReferenceType... args)
    {
        if (typeVariables.isEmpty())
        {
            throw new UnsupportedOperationException("Not a generic type");
        }

        return parameterize(createTypeVarMap(typeVariables, Arrays.asList(args)));
    }

    public JavaParameterizedType<M> parameterize(
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        if (!hasTypeVariables())
        {
            throw new UnsupportedOperationException("Not a generic type");
        }

        final List<JavaTypeVariable> fullTypeVars = getFullTypeVariables();
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap = restrictTypeVarMap(
            fullTypeVarMap, fullTypeVars);
        addTypeArgWildcards(typeVarMap, fullTypeVars);
        JavaParameterizedType<M> result = lookupParameterization(typeVarMap);
        if (result == null)
        {
            result = createParameterization(typeVarMap);
            cacheParameterization(typeVarMap, result);
            resolveParameterization(result, fullTypeVarMap);
        }
        return result;
    }

    public void addBaseInterface(JavaAbstractInterface baseInterface)
    {
        super.addBaseInterface(baseInterface);
    }

    public final void addMember(M member)
    {
        super.addMember(member);
    }

    public final void addMemberBefore(M member, JavaStructuredTypeMember other)
    {
        super.addMemberBefore(member, other);
    }

    public final void addMemberAfter(M member, JavaStructuredTypeMember other)
    {
        super.addMemberAfter(member, other);
    }

    public String toReferenceString()
    {
        return name.getCanonicalName();
    }

    public String toInternalName()
    {
        return "L" + name.getCanonicalName() + ";";
    }

    public void accept(JavaSchemaMemberVisitor visitor)
    {
        // ignored by default
    }

    public void accept(JavaPackageMemberVisitor visitor)
    {
        // ignored by default
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        // ignored by default
    }
}
