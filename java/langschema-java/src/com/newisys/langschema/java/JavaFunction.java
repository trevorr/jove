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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.newisys.langschema.Function;

/**
 * Represents a Java method.
 * 
 * @author Trevor Robinson
 */
public final class JavaFunction
    extends JavaFunctor
    implements Function, JavaClassMember, JavaInterfaceMember
{
    private final JavaName name;
    private final EnumSet<JavaFunctionModifier> modifiers = EnumSet
        .noneOf(JavaFunctionModifier.class);
    private JavaAnnotationElementValue defaultValue;

    public JavaFunction(String id, JavaFunctionType funcType)
    {
        super(funcType);
        this.name = new JavaName(id, JavaNameKind.METHOD);
    }

    final void copyFrom(JavaFunction other)
    {
        super.copyFrom(other);
        modifiers.addAll(other.modifiers);
    }

    public JavaFunction clone()
    {
        final JavaFunction clone = new JavaFunction(name.getIdentifier(),
            funcType);
        clone.copyFrom(this);
        return clone;
    }

    public JavaName getName()
    {
        return name;
    }

    public void setStructuredType(JavaStructuredType container)
    {
        super.setStructuredType(container);
        name.setNamespace(container);
    }

    public Set<JavaFunctionModifier> getModifiers()
    {
        return modifiers;
    }

    public boolean hasModifier(JavaFunctionModifier modifier)
    {
        return modifiers.contains(modifier);
    }

    public void addModifier(JavaFunctionModifier modifier)
    {
        modifiers.add(modifier);
    }

    public void addModifiers(Set<JavaFunctionModifier> mods)
    {
        modifiers.addAll(mods);
    }

    public JavaAnnotationElementValue getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(JavaAnnotationElementValue defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public void checkValidOverride(JavaFunction base)
        throws JavaSemanticException
    {
        // matching signature should have been already established
        assert (signatureMatches(base));

        // check final
        if (base.getModifiers().contains(JavaFunctionModifier.FINAL))
        {
            throw new JavaSemanticException("Cannot override final method '"
                + base + "'");
        }

        // check visibility
        if (visibility.isLessVisible(base.getVisibility()))
        {
            throw new JavaSemanticException("Overriding method '" + this
                + "' cannot have less visibility than base method '" + base
                + "'");
        }

        // check return type
        final JavaType baseReturnType = base.funcType.getReturnType();
        final JavaType returnType = funcType.getReturnType();
        if (baseReturnType instanceof JavaPrimitiveType
            || baseReturnType instanceof JavaVoidType)
        {
            if (!baseReturnType.equals(returnType))
            {
                throw new JavaSemanticException("Overriding method '" + this
                    + "' has different return type than base method '" + base
                    + "'");
            }
        }
        else
        {
            if (!baseReturnType.isAssignableFrom(returnType))
            {
                throw new JavaSemanticException(
                    "Overriding method '"
                        + this
                        + "' has return type not assignable to return type of base method '"
                        + base + "'");
            }
        }

        // check throws:
        // get exceptions thrown by overriding method not declared in base
        final Set<JavaClass> exceptions = new HashSet<JavaClass>(funcType
            .getExceptionTypes());
        exceptions.removeAll(base.funcType.getExceptionTypes());
        // remove non-checked exceptions (derived from RuntimeException)
        Iterator<JavaClass> iter = exceptions.iterator();
        while (iter.hasNext())
        {
            JavaAbstractClass exception = iter.next();
            while (exception != null)
            {
                if (exception.getName().getCanonicalName().equals(
                    "java.lang.RuntimeException"))
                {
                    iter.remove();
                    break;
                }
                exception = exception.getBaseClass();
            }
        }
        // any remaining exceptions are illegal
        if (!exceptions.isEmpty())
        {
            throw new JavaSemanticException("Overriding method '" + this
                + "' throws checked exception(s) not declared in base method '"
                + base + "': " + exceptions);
        }
    }

    public void checkValidInInterface()
        throws JavaSemanticException
    {
        // check visibility
        if (visibility != JavaVisibility.PUBLIC
            && visibility != JavaVisibility.DEFAULT)
        {
            throw new JavaSemanticException("Interface method '" + name
                + "' cannot be declared with " + visibility + " visibility");
        }

        // check modifiers
        final EnumSet<JavaFunctionModifier> illegal = EnumSet.of(
            JavaFunctionModifier.STATIC, JavaFunctionModifier.FINAL,
            JavaFunctionModifier.SYNCHRONIZED, JavaFunctionModifier.NATIVE,
            JavaFunctionModifier.STRICTFP);
        illegal.retainAll(modifiers);
        if (!illegal.isEmpty())
        {
            throw new JavaSemanticException("Interface method '" + name
                + "' cannot have modifiers " + illegal);
        }
    }

    public void checkValidInAnnotation(JavaAnnotationType annotation)
        throws JavaSemanticException
    {
        checkValidInInterface();

        // no type arguments allowed
        if (!funcType.getTypeVariables().isEmpty())
        {
            throw new JavaSemanticException("Annotation method '" + name
                + "' cannot have type arguments");
        }

        // no arguments allowed
        if (!funcType.getArguments().isEmpty())
        {
            throw new JavaSemanticException("Annotation method '" + name
                + "' cannot have arguments");
        }

        // no exception declarations allowed
        if (!funcType.getExceptionTypes().isEmpty())
        {
            throw new JavaSemanticException("Annotation method '" + name
                + "' cannot have a throws clause");
        }

        // check return type
        final JavaType returnType = funcType.getReturnType();
        if (!(isValidAnnotationType(returnType)))
        {
            throw new JavaSemanticException("Annotation method '" + name
                + "' cannot have return type '"
                + returnType.toReferenceString() + "'");
        }

        // TODO: type cannot refer (even indirectly) to this annotation

        // TODO: cannot override methods in Object or Annotation

        // check default value type
        if (defaultValue != null && !isCommensurate(returnType, defaultValue))
        {
            throw new JavaSemanticException("Default value '" + defaultValue
                + "' of annotation method '" + name
                + "' is not commensurate with return type '"
                + returnType.toReferenceString() + "'");

        }
    }

    private boolean isValidAnnotationType(JavaType type)
    {
        return isValidAnnotationElementType(type)
            || (type instanceof JavaArrayType && isValidAnnotationArrayType((JavaArrayType) type));
    }

    private boolean isValidAnnotationArrayType(JavaArrayType type)
    {
        return type.getIndexTypes().length == 1
            && isValidAnnotationElementType(type.getElementType());
    }

    private boolean isValidAnnotationElementType(JavaType type)
    {
        return type instanceof JavaPrimitiveType
            || type == schema.getStringType()
            || schema.getClassType().isAssignableFrom(type)
            || schema.getEnumType().isAssignableFrom(type)
            || schema.getAnnotationType().isAssignableFrom(type);
    }

    public boolean isCommensurate(JavaAnnotationElementValue value)
    {
        return isCommensurate(funcType.getReturnType(), value);
    }

    private boolean isCommensurate(
        JavaType type,
        JavaAnnotationElementValue value)
    {
        if (type instanceof JavaArrayType)
        {
            final JavaArrayType arrayType = (JavaArrayType) type;
            final JavaType elemType = arrayType.getElementType();
            if (value instanceof JavaArrayInitializer)
            {
                final JavaArrayInitializer init = (JavaArrayInitializer) value;
                for (final JavaExpression elem : init.getElements())
                {
                    if (!isCommensurateElement(elemType, elem)) return false;
                }
                return true;
            }
            else
            {
                return isCommensurateElement(elemType, value);
            }
        }
        else
        {
            return isCommensurateElement(type, value);
        }
    }

    private boolean isCommensurateElement(
        JavaType type,
        JavaAnnotationElementValue value)
    {
        if (!type.isAssignableFrom(value.getResultType())) return false;
        if (value instanceof JavaNullLiteral) return false;
        if (type instanceof JavaPrimitiveType || type == schema.getStringType())
            return value instanceof JavaExpression
                && ((JavaExpression) value).isConstant();
        if (schema.getClassType().isAssignableFrom(type))
            return value instanceof JavaTypeLiteral;
        if (schema.getEnumType().isAssignableFrom(type))
            return value instanceof JavaVariableReference;
        return true;
    }

    public boolean isMoreSpecific(JavaFunctor other)
    {
        // this method's type must be convertible to other method's type,
        // in addition to the argument types being convertible
        return other.container.isAssignableFrom(container)
            && super.isMoreSpecific(other);
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return "function " + name;
    }
}
