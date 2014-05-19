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
import java.util.Set;

import com.newisys.langschema.FunctionType;
import com.newisys.langschema.java.*;

/**
 * Printer module used to print Java block members. Instances of this module
 * are created on the fly, since they depend on the type and package context.
 * 
 * @author Trevor Robinson
 */
public class TypeMemberPrinter
    extends JavaSchemaPrinterModule
    implements JavaStructuredTypeMemberVisitor
{
    private final JavaStructuredType< ? > type;
    private final JavaPackage pkg;

    private final boolean isIntf;
    private final JavaVisibility defaultVis;

    public TypeMemberPrinter(
        BasePrinter basePrinter,
        JavaStructuredType< ? > type)
    {
        super(basePrinter);
        this.type = type;
        this.pkg = type.getPackage();

        isIntf = type instanceof JavaAbstractInterface;
        defaultVis = isIntf ? JavaVisibility.PUBLIC : JavaVisibility.DEFAULT;
    }

    private void printBody(JavaBlock body)
    {
        if (getConfig().isCollapseBodies())
        {
            fmt.printTrailingToken("...");
            printNewLine();
        }
        else
        {
            printNewLine();
            basePrinter.printBlock(body, type);
        }
    }

    public void printMembers(
        Collection< ? extends JavaStructuredTypeMember> members)
    {
        JavaStructuredTypeMember prev = null;
        for (final JavaStructuredTypeMember member : members)
        {
            if (prev != null
                && !(prev instanceof JavaMemberVariable && member instanceof JavaMemberVariable))
            {
                printNewLine();
            }
            member.accept(this);
            prev = member;
        }
    }

    public void visit(JavaAnnotationType obj)
    {
        basePrinter.printAnnotationType(obj);
    }

    public void visit(JavaClass obj)
    {
        basePrinter.printClass(obj);
    }

    public void visit(JavaEnum obj)
    {
        basePrinter.printEnum(obj);
    }

    public void visit(JavaConstructor obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printAnnotations(obj, pkg, false);
        basePrinter.printVisibility(obj.getVisibility());
        fmt.printToken(type.getName().getIdentifier());
        basePrinter.printFuncArgs(obj.getType(), pkg);
        printBody(obj.getBody());
        basePrinter.printComments(obj, false);
    }

    private static final EnumSet<JavaFunctionModifier> INTF_FUNC_EXCL = EnumSet
        .of(JavaFunctionModifier.ABSTRACT);

    public void visit(JavaFunction obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printAnnotations(obj, pkg, false);
        basePrinter.printVisibility(obj.getVisibility(), defaultVis);
        final Set<JavaFunctionModifier> modifiers = obj.getModifiers();
        basePrinter.printModifiers(modifiers, JavaFunctionModifier.class,
            isIntf ? INTF_FUNC_EXCL : null);
        final FunctionType funcType = obj.getType();
        basePrinter.printType((JavaType) funcType.getReturnType(), pkg);
        fmt.printSpace();
        fmt.printToken(obj.getName().getIdentifier());
        basePrinter.printFuncArgs((JavaFunctionType) funcType, pkg);
        final JavaAnnotationElementValue defaultValue = obj.getDefaultValue();
        if (defaultValue != null)
        {
            fmt.printSpace();
            fmt.printToken("default");
            fmt.printSpace();
            basePrinter.printAnnotationValue(defaultValue, type, pkg);
        }
        if (modifiers.contains(JavaFunctionModifier.ABSTRACT)
            || modifiers.contains(JavaFunctionModifier.NATIVE))
        {
            fmt.printTrailingToken(";");
            printNewLine();
        }
        else
        {
            printBody(obj.getBody());
        }
        basePrinter.printComments(obj, false);
    }

    public void visit(JavaInitializerBlock obj)
    {
        basePrinter.printComments(obj, true);

        if (obj.isStatic())
        {
            fmt.printToken("static");
            printNewLine();
        }

        if (getConfig().isCollapseBodies())
        {
            fmt.printToken("{");
            fmt.printSpace();
            fmt.printToken("...");
            fmt.printSpace();
            fmt.printToken("}");
        }
        else
        {
            basePrinter.printBlockPlain(obj, type);
        }

        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }

    public void visit(JavaInterface obj)
    {
        basePrinter.printInterface(obj);
    }

    public void visit(JavaMemberVariable obj)
    {
        basePrinter.printComments(obj, true);
        basePrinter.printAnnotations(obj, pkg, false);
        basePrinter.printVisibility(obj.getVisibility(), defaultVis);
        basePrinter.printVarDecl(obj, pkg);
        basePrinter.printVarInit(obj, type);
        fmt.printTrailingToken(";");
        basePrinter.printComments(obj, false);
        if (!fmt.isNewLine()) printNewLine();
    }
}
