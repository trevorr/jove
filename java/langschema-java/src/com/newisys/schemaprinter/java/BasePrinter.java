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

package com.newisys.schemaprinter.java;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Annotation;
import com.newisys.langschema.BlankLine;
import com.newisys.langschema.BlockComment;
import com.newisys.langschema.InlineComment;
import com.newisys.langschema.Name;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.SchemaObject;
import com.newisys.langschema.Visibility;
import com.newisys.langschema.java.*;
import com.newisys.schemaprinter.SchemaPrinterModule;
import com.newisys.util.text.TokenFormatter;

/**
 * Base printer module used to print objects common to all modules.
 * 
 * @author Trevor Robinson
 */
public class BasePrinter
    extends SchemaPrinterModule
{
    protected final JavaSchemaPrinter config;
    protected final ImportManager importMgr;

    public BasePrinter(
        TokenFormatter fmt,
        JavaSchemaPrinter config,
        ImportManager importMgr)
    {
        super(fmt);
        this.config = config;
        this.importMgr = importMgr;
    }

    public TokenFormatter getFormatter()
    {
        return fmt;
    }

    public JavaSchemaPrinter getConfig()
    {
        return config;
    }

    public boolean printComments(SchemaObject obj, boolean leading)
    {
        return printComments(obj, leading, false);
    }

    public boolean printComments(
        SchemaObject obj,
        boolean leading,
        boolean allowLeadingBlank)
    {
        boolean found = false;
        boolean prevBlank = !allowLeadingBlank;
        final Iterator< ? extends Annotation> iter = obj.getAnnotations()
            .iterator();
        while (iter.hasNext())
        {
            Annotation ann = iter.next();
            if (ann.isLeading() != leading) continue;

            if (ann instanceof BlockComment)
            {
                BlockComment comment = (BlockComment) ann;
                for (final String line : comment.getLines())
                {
                    if (!leading) fmt.printSpace();
                    fmt.printToken("//" + line);
                    printNewLine();
                }
                prevBlank = false;
                found = true;
            }
            else if (ann instanceof InlineComment)
            {
                InlineComment comment = (InlineComment) ann;
                if (!leading) fmt.printSpace();
                fmt.printToken("/*" + comment.getText() + "*/");
                if (leading) fmt.printSpace();
                prevBlank = false;
                found = true;
            }
            else if (ann instanceof BlankLine)
            {
                if (!prevBlank) printNewLine();
                prevBlank = true;
                found = true;
            }
        }
        return found;
    }

    public void printID(Name name)
    {
        fmt.printToken(name.getIdentifier());
    }

    public void printName(Name name, JavaPackage pkgContext)
    {
        String id = name.getIdentifier();
        Namespace namespace = name.getNamespace();
        if (namespace != null && namespace != pkgContext)
        {
            // do not attempt to import names defined in the current package
            if ((pkgContext != null && pkgContext.lookupObjects(id,
                name.getKind()).hasNext())
                || !importMgr.isImported(name))
            {
                printNamespace(namespace);
            }
        }
        fmt.printToken(id);
    }

    private void printNamespace(Namespace namespace)
    {
        printNameNoImport(namespace.getName());
        fmt.printLeadingToken(".");
    }

    private void printNameNoImport(Name name)
    {
        Namespace namespace = name.getNamespace();
        if (namespace != null)
        {
            printNamespace(namespace);
        }
        fmt.printToken(name.getIdentifier());
    }

    private void printNames(
        Collection< ? extends NamedObject> objs,
        JavaPackage pkgContext)
    {
        Iterator< ? extends NamedObject> iter = objs.iterator();
        while (iter.hasNext())
        {
            NamedObject obj = iter.next();
            printName(obj.getName(), pkgContext);
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }
    }

    public void printVisibility(Visibility vis, Visibility def)
    {
        if (vis != def)
        {
            fmt.printToken(vis.toString());
            fmt.printSpace();
        }
    }

    public void printVisibility(Visibility vis)
    {
        printVisibility(vis, JavaVisibility.DEFAULT);
    }

    public <E extends Enum> void printModifiers(
        Set<E> modifiers,
        Class<E> modifierClass,
        Set<E> exclusions)
    {
        for (E modifier : modifierClass.getEnumConstants())
        {
            if (modifiers.contains(modifier)
                && (exclusions == null || !exclusions.contains(modifier)))
            {
                fmt.printToken(modifier.toString());
                fmt.printSpace();
            }
        }
    }

    public <E extends Enum> void printModifiers(
        Set<E> modifiers,
        Class<E> modifierClass)
    {
        printModifiers(modifiers, modifierClass, null);
    }

    public void printAnnotation(JavaAnnotation ann, JavaPackage pkgContext)
    {
        fmt.printLeadingToken("@");
        final JavaAnnotationType type = ann.getType();
        printName(type.getName(), pkgContext);
        final List<JavaFunction> elems = type.getElements();
        if (!elems.isEmpty())
        {
            fmt.printTrailingToken("(");
            if (elems.size() == 1)
            {
                final JavaFunction elem = elems.get(0);
                final JavaAnnotationElementValue value = ann
                    .getAssignedElementValue(elem);
                if (value != null)
                {
                    if (elem.getName().getIdentifier().equals("value"))
                    {
                        printAnnotationValue(value, null, pkgContext);
                    }
                    else
                    {
                        printAnnotationElement(elem, value, pkgContext);
                    }
                }
            }
            else
            {
                boolean first = true;
                for (final JavaFunction elem : elems)
                {
                    final JavaAnnotationElementValue value = ann
                        .getAssignedElementValue(elem);
                    if (value == null) continue;
                    if (!first)
                    {
                        fmt.printTrailingToken(",");
                        fmt.printSpace();
                    }
                    printAnnotationElement(elem, value, pkgContext);
                    first = false;
                }
            }
            fmt.printTrailingToken(")");
        }
    }

    private void printAnnotationElement(
        JavaFunction elem,
        JavaAnnotationElementValue value,
        JavaPackage pkgContext)
    {
        printID(elem.getName());
        fmt.printSpace();
        fmt.printToken("=");
        fmt.printSpace();
        printAnnotationValue(value, null, pkgContext);
    }

    public void printAnnotationValue(
        JavaAnnotationElementValue value,
        JavaStructuredType< ? > typeContext,
        JavaPackage pkgContext)
    {
        if (value instanceof JavaExpression)
        {
            JavaExpression expr = (JavaExpression) value;
            printExpression(expr, typeContext, pkgContext, 100);
        }
        else if (value instanceof JavaAnnotation)
        {
            JavaAnnotation ann = (JavaAnnotation) value;
            printAnnotation(ann, pkgContext);
        }
        else if (value instanceof JavaAnnotationArrayInitializer)
        {
            JavaAnnotationArrayInitializer arr = (JavaAnnotationArrayInitializer) value;
            printAnnotationArrayInitializer(arr, typeContext, pkgContext);
        }
        else
        {
            throw new ClassCastException(
                "Unknown annotation element value type: "
                    + value.getClass().getName());
        }
    }

    private void printAnnotationArrayInitializer(
        JavaAnnotationArrayInitializer arr,
        JavaStructuredType< ? > typeContext,
        JavaPackage pkgContext)
    {
        final List<JavaAnnotationElementValue> elems = arr.getElements();
        if (elems.size() == 1)
        {
            final JavaAnnotationElementValue elem = elems.get(0);
            printAnnotationValue(elem, typeContext, pkgContext);
        }
        else
        {
            fmt.printLeadingToken("{");
            final Iterator<JavaAnnotationElementValue> iter = elems.iterator();
            while (iter.hasNext())
            {
                fmt.printSpace();
                final JavaAnnotationElementValue elem = iter.next();
                printAnnotationValue(elem, typeContext, pkgContext);
                if (iter.hasNext()) fmt.printTrailingToken(",");
            }
            fmt.printSpace();
            fmt.printTrailingToken("}");
        }
    }

    public boolean printAnnotations(
        SchemaObject obj,
        JavaPackage pkgContext,
        boolean newLine)
    {
        boolean found = false;
        final Iterator< ? extends Annotation> iter = obj.getAnnotations()
            .iterator();
        while (iter.hasNext())
        {
            final Annotation ann = iter.next();
            if (ann instanceof JavaAnnotation)
            {
                JavaAnnotation jann = (JavaAnnotation) ann;
                printAnnotation(jann, pkgContext);
                if (newLine)
                    printNewLine();
                else
                    fmt.printSpace();
                found = true;
            }
        }
        return found;
    }

    public void printType(JavaType type, JavaPackage pkgContext)
    {
        if (type instanceof JavaArrayType)
        {
            JavaArrayType arrayType = (JavaArrayType) type;
            printType(arrayType.getElementType(), pkgContext);

            int dimCount = arrayType.getIndexTypes().length;
            for (int i = 0; i < dimCount; ++i)
            {
                fmt.printTrailingToken("[]");
            }
        }
        else if (type instanceof JavaParameterizedType)
        {
            JavaParameterizedType< ? > paramType = (JavaParameterizedType) type;
            printType(paramType.getRawType(), pkgContext);
            final List<JavaReferenceType> typeArgs = paramType
                .getActualTypeArguments();
            if (!typeArgs.isEmpty())
            {
                boolean first = true;
                fmt.printLeadingToken("<");
                for (JavaType typeArg : typeArgs)
                {
                    if (!first)
                    {
                        fmt.printTrailingToken(",");
                        fmt.printSpace();
                    }
                    printType(typeArg, pkgContext);
                    first = false;
                }
                fmt.printTrailingToken(">");
            }
        }
        else if (type instanceof JavaWildcardType)
        {
            final JavaWildcardType wildType = (JavaWildcardType) type;
            fmt.printToken("?");
            final List<JavaTypeBound> ub = wildType.getUpperBounds();
            if (!ub.isEmpty())
            {
                fmt.printSpace();
                fmt.printToken("extends");
                fmt.printSpace();
                printTypes(ub, pkgContext);
            }
            final List<JavaTypeBound> lb = wildType.getLowerBounds();
            if (!lb.isEmpty())
            {
                fmt.printSpace();
                fmt.printToken("super");
                fmt.printSpace();
                printTypes(lb, pkgContext);
            }
        }
        else if (type instanceof NamedObject)
        {
            printName(((NamedObject) type).getName(), pkgContext);
        }
        else
        {
            fmt.printToken(type.toReferenceString());
        }
    }

    private void printTypes(
        Collection< ? extends JavaType> types,
        JavaPackage pkgContext)
    {
        Iterator< ? extends JavaType> iter = types.iterator();
        while (iter.hasNext())
        {
            JavaType type = iter.next();
            printType(type, pkgContext);
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }
    }

    public void printVarDecl(JavaVariable var, JavaPackage pkgContext)
    {
        printModifiers(var.getModifiers(), JavaVariableModifier.class);
        printType(var.getType(), pkgContext);
        fmt.printSpace();
        printID(var.getName());
    }

    public void printVarInit(JavaVariable arg, JavaStructuredType typeContext)
    {
        JavaExpression initExpr = arg.getInitializer();
        if (initExpr != null)
        {
            fmt.printSpace();
            fmt.printLeadingToken("=");
            fmt.printSpace();
            printExpression(initExpr, typeContext);
        }
    }

    public void printFuncArgs(JavaFunctionType funcType, JavaPackage pkgContext)
    {
        fmt.printTrailingToken("(");
        Iterator<JavaFunctionArgument> iter = funcType.getArguments()
            .iterator();
        while (iter.hasNext())
        {
            JavaFunctionArgument arg = iter.next();
            printComments(arg, true);
            printAnnotations(arg, pkgContext, false);
            printVarDecl(arg, pkgContext);
            printComments(arg, false);
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }
        fmt.printTrailingToken(")");
        Set<JavaClass> exceptionTypes = funcType.getExceptionTypes();
        if (!exceptionTypes.isEmpty())
        {
            printNewLine();
            fmt.incIndent();
            fmt.printToken("throws");
            fmt.printSpace();
            printNames(exceptionTypes, pkgContext);
            fmt.decIndent();
        }
    }

    protected ExpressionPrinter getExpressionPrinter(
        JavaStructuredType< ? > typeContext,
        JavaPackage pkgContext,
        int parentPrecedence)
    {
        return new ExpressionPrinter(this, typeContext, pkgContext,
            parentPrecedence);
    }

    public void printExpression(
        JavaExpression expr,
        JavaStructuredType< ? > typeContext,
        JavaPackage pkgContext,
        int parentPrecedence)
    {
        ExpressionPrinter printer = getExpressionPrinter(typeContext,
            pkgContext, parentPrecedence);
        fmt.beginGroup();
        printComments(expr, true, true);
        expr.accept(printer);
        printComments(expr, false);
        fmt.endGroup();
    }

    public void printExpression(
        JavaExpression expr,
        JavaStructuredType< ? > typeContext,
        int parentPrecedence)
    {
        printExpression(expr, typeContext, typeContext != null ? typeContext
            .getPackage() : null, parentPrecedence);
    }

    public void printExpression(
        JavaExpression expr,
        JavaStructuredType typeContext)
    {
        printExpression(expr, typeContext, 100);
    }

    public void printExpression(JavaExpression expr, JavaPackage pkgContext)
    {
        printExpression(expr, null, pkgContext, 100);
    }

    public void printArgs(
        List<JavaExpression> args,
        JavaStructuredType typeContext)
    {
        fmt.printTrailingToken("(");
        final Iterator<JavaExpression> iter = args.iterator();
        while (iter.hasNext())
        {
            printExpression(iter.next(), typeContext);
            if (iter.hasNext())
            {
                fmt.printTrailingToken(",");
                fmt.printSpace();
            }
        }
        fmt.printTrailingToken(")");
    }

    protected BlockMemberPrinter getBlockMemberPrinter(
        JavaStructuredType typeContext)
    {
        return new BlockMemberPrinter(this, typeContext);
    }

    public void printBlock(JavaBlock block, JavaStructuredType typeContext)
    {
        printBlock(block, typeContext, false);
    }

    public void printBlock(
        JavaBlock block,
        JavaStructuredType typeContext,
        boolean allowLeadingBlank)
    {
        if (block != null) printComments(block, true, allowLeadingBlank);
        printBlockPlain(block, typeContext);
        if (block != null) printComments(block, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void printBlockPlain(JavaBlock block, JavaStructuredType typeContext)
    {
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        if (block != null)
        {
            BlockMemberPrinter visitor = getBlockMemberPrinter(typeContext);
            visitor.printMembers(block.getMembers());
        }
        else
        {
            fmt.printToken("// NULL BLOCK");
            printNewLine();
        }

        fmt.decIndent();
        fmt.printToken("}");
    }

    public void printClass(JavaClass obj)
    {
        JavaPackage pkg = obj.getPackage();

        printComments(obj, true);
        printAnnotations(obj, pkg, true);
        printVisibility(obj.getVisibility());
        printModifiers(obj.getModifiers(), JavaTypeModifier.class);
        fmt.printToken("class");
        fmt.printSpace();
        printID(obj.getName());
        printTypeVariables(obj, pkg);
        printNewLine();

        JavaAbstractClass baseCls = obj.getBaseClass();
        if (baseCls != null && baseCls != baseCls.getSchema().getObjectType())
        {
            fmt.incIndent();
            fmt.printToken("extends");
            fmt.printSpace();
            printType(baseCls, pkg);
            printNewLine();
            fmt.decIndent();
        }

        List<JavaAbstractInterface> baseIntfs = ((JavaStructuredType< ? >) obj)
            .getBaseInterfaces();
        if (!baseIntfs.isEmpty())
        {
            fmt.incIndent();
            fmt.printToken("implements");
            fmt.printSpace();
            printTypes(baseIntfs, pkg);
            printNewLine();
            fmt.decIndent();
        }

        printTypeBody(obj);
        printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    private static final EnumSet<JavaTypeModifier> ENUM_EXCL = EnumSet.of(
        JavaTypeModifier.FINAL, JavaTypeModifier.STATIC);

    public void printEnum(JavaEnum obj)
    {
        final JavaPackage pkg = obj.getPackage();

        printComments(obj, true);
        printAnnotations(obj, pkg, true);
        printVisibility(obj.getVisibility());
        printModifiers(obj.getModifiers(), JavaTypeModifier.class, ENUM_EXCL);
        fmt.printToken("enum");
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();

        final List<JavaAbstractInterface> baseIntfs = obj.getBaseInterfaces();
        if (!baseIntfs.isEmpty())
        {
            fmt.incIndent();
            fmt.printToken("implements");
            fmt.printSpace();
            printTypes(baseIntfs, pkg);
            printNewLine();
            fmt.decIndent();
        }

        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        final List<JavaMemberVariable> values = obj.getValues();
        final Iterator<JavaMemberVariable> valueIter = values.iterator();
        while (valueIter.hasNext())
        {
            final JavaMemberVariable value = valueIter.next();
            final JavaType valueType = value.getType();
            final boolean hasBody = valueType != obj;
            if (!fmt.isNewLine())
            {
                if (hasBody)
                    printNewLine();
                else
                    fmt.printSpace();
            }
            printComments(value, true);
            printAnnotations(value, pkg, false);
            printID(value.getName());
            final JavaExpression initExpr = value.getInitializer();
            if (initExpr != null)
            {
                final JavaInstanceCreation initNew = (JavaInstanceCreation) initExpr;
                printArgs(initNew.getArguments(), obj);
            }
            if (hasBody)
            {
                fmt.printSpace();
                printTypeBody((JavaAbstractClass) valueType);
            }
            fmt.printTrailingToken(valueIter.hasNext() ? "," : ";");
            printComments(value, false);
            if (hasBody && !fmt.isNewLine()) printNewLine();
        }
        if (!fmt.isNewLine()) printNewLine();

        final List<JavaClassMember> members = obj.getMembers();
        if (!members.isEmpty())
        {
            printNewLine();
            final TypeMemberPrinter visitor = getTypeMemberPrinter(obj);
            JavaClassMember prev = null;
            for (final JavaClassMember member : members)
            {
                // filter out enum constants
                if (values.contains(member)) continue;

                if (prev != null
                    && !(prev instanceof JavaMemberVariable && member instanceof JavaMemberVariable))
                {
                    printNewLine();
                }
                member.accept(visitor);
                prev = member;
            }
        }

        fmt.decIndent();
        fmt.printToken("}");
        printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    private static final EnumSet<JavaTypeModifier> INTF_EXCL = EnumSet.of(
        JavaTypeModifier.ABSTRACT, JavaTypeModifier.STATIC);

    public void printInterface(JavaInterface obj)
    {
        JavaPackage pkg = obj.getPackage();

        printComments(obj, true);
        printAnnotations(obj, pkg, true);
        printVisibility(obj.getVisibility());
        printModifiers(obj.getModifiers(), JavaTypeModifier.class, INTF_EXCL);
        fmt.printToken("interface");
        fmt.printSpace();
        printID(obj.getName());
        printTypeVariables(obj, pkg);
        printNewLine();

        List<JavaAbstractInterface> baseIntfs = ((JavaStructuredType< ? >) obj)
            .getBaseInterfaces();
        if (!baseIntfs.isEmpty())
        {
            fmt.incIndent();
            fmt.printToken("extends");
            fmt.printSpace();
            printTypes(baseIntfs, pkg);
            printNewLine();
            fmt.decIndent();
        }

        printTypeBody(obj);
        printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void printAnnotationType(JavaAnnotationType obj)
    {
        JavaPackage pkg = obj.getPackage();

        printComments(obj, true);
        printAnnotations(obj, pkg, true);
        printVisibility(obj.getVisibility());
        printModifiers(obj.getModifiers(), JavaTypeModifier.class, INTF_EXCL);
        fmt.printLeadingToken("@");
        fmt.printToken("interface");
        fmt.printSpace();
        printID(obj.getName());
        printNewLine();

        printTypeBody(obj);
        printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void printTypeVariables(
        JavaGenericDeclaration obj,
        JavaPackage pkgContext)
    {
        final List<JavaTypeVariable> typeVars = obj.getTypeVariables();
        if (!typeVars.isEmpty())
        {
            fmt.printLeadingToken("<");
            boolean first = true;
            for (JavaTypeVariable typeVar : typeVars)
            {
                if (!first)
                {
                    fmt.printTrailingToken(",");
                    fmt.printSpace();
                }
                printTypeVariable(typeVar, pkgContext);
                first = false;
            }
            fmt.printTrailingToken(">");
        }
    }

    public void printTypeVariable(JavaTypeVariable obj, JavaPackage pkgContext)
    {
        printID(obj.getName());
        boolean first = true;
        for (JavaType bound : obj.getUpperBounds())
        {
            fmt.printSpace();
            fmt.printToken(first ? "extends" : "&");
            fmt.printSpace();
            printType(bound, pkgContext);
            first = false;
        }
    }

    protected TypeMemberPrinter getTypeMemberPrinter(
        JavaStructuredType container)
    {
        return new TypeMemberPrinter(this, container);
    }

    public void printTypeBody(JavaStructuredType< ? > obj)
    {
        fmt.printToken("{");
        printNewLine();
        fmt.incIndent();

        TypeMemberPrinter visitor = getTypeMemberPrinter(obj);
        visitor.printMembers(obj.getMembers());

        fmt.decIndent();
        fmt.printToken("}");
    }
}
