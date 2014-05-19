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

import com.newisys.langschema.java.*;

/**
 * Printer module used to print all first-class schema objects (i.e. those
 * appearing in JavaSchemaObjectVisitor).
 * 
 * @author Trevor Robinson
 */
public class SchemaObjectPrinter
    extends JavaSchemaPrinterModule
    implements JavaSchemaObjectVisitor
{
    public SchemaObjectPrinter(BasePrinter basePrinter)
    {
        super(basePrinter);
    }

    protected final void print(JavaSchemaMember obj)
    {
        StreamPackageMemberPrinter printer = new StreamPackageMemberPrinter(
            basePrinter);
        obj.accept(printer);
    }

    protected final void print(JavaStructuredTypeMember obj)
    {
        TypeMemberPrinter printer = new TypeMemberPrinter(basePrinter, obj
            .getStructuredType());
        obj.accept(printer);
    }

    protected final void print(JavaBlockMember obj)
    {
        BlockMemberPrinter printer = new BlockMemberPrinter(basePrinter, null);
        obj.accept(printer);
    }

    protected final void print(JavaExpression obj)
    {
        ExpressionPrinter printer = new ExpressionPrinter(basePrinter, null,
            null, 100);
        obj.accept(printer);
    }

    protected final void print(JavaType obj)
    {
        basePrinter.printType(obj, null);
    }

    public void visit(JavaAdd obj)
    {
        print(obj);
    }

    public void visit(JavaAnd obj)
    {
        print(obj);
    }

    public void visit(JavaAnnotation obj)
    {
        basePrinter.printAnnotation(obj, null);
    }

    public void visit(JavaAnnotationArrayInitializer obj)
    {
        basePrinter.printAnnotationValue(obj, null, null);
    }

    public void visit(JavaAnnotationType obj)
    {
        print((JavaSchemaMember) obj);
    }

    public void visit(JavaArrayAccess obj)
    {
        print(obj);
    }

    public void visit(JavaArrayCreation obj)
    {
        print(obj);
    }

    public void visit(JavaArrayInitializer obj)
    {
        print(obj);
    }

    public void visit(JavaArrayType obj)
    {
        print((JavaSchemaMember) obj);
    }

    public void visit(JavaAssertStatement obj)
    {
        print(obj);
    }

    public void visit(JavaAssign obj)
    {
        print(obj);
    }

    public void visit(JavaAssignAdd obj)
    {
        print(obj);
    }

    public void visit(JavaAssignAnd obj)
    {
        print(obj);
    }

    public void visit(JavaAssignDivide obj)
    {
        print(obj);
    }

    public void visit(JavaAssignLeftShift obj)
    {
        print(obj);
    }

    public void visit(JavaAssignModulo obj)
    {
        print(obj);
    }

    public void visit(JavaAssignMultiply obj)
    {
        print(obj);
    }

    public void visit(JavaAssignOr obj)
    {
        print(obj);
    }

    public void visit(JavaAssignSignedRightShift obj)
    {
        print(obj);
    }

    public void visit(JavaAssignSubtract obj)
    {
        print(obj);
    }

    public void visit(JavaAssignUnsignedRightShift obj)
    {
        print(obj);
    }

    public void visit(JavaAssignXor obj)
    {
        print(obj);
    }

    public void visit(JavaBitwiseNot obj)
    {
        print(obj);
    }

    public void visit(JavaBlock obj)
    {
        print(obj);
    }

    public void visit(JavaBooleanLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaBooleanType obj)
    {
        print(obj);
    }

    public void visit(JavaBreakStatement obj)
    {
        print(obj);
    }

    public void visit(JavaByteType obj)
    {
        print(obj);
    }

    public void visit(JavaCastExpression obj)
    {
        print(obj);
    }

    public void visit(JavaCharLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaCharType obj)
    {
        print(obj);
    }

    public void visit(JavaClass obj)
    {
        print((JavaSchemaMember) obj);
    }

    public void visit(JavaConditional obj)
    {
        print(obj);
    }

    public void visit(JavaConditionalAnd obj)
    {
        print(obj);
    }

    public void visit(JavaConditionalOr obj)
    {
        print(obj);
    }

    public void visit(JavaConstructor obj)
    {
        print(obj);
    }

    public void visit(JavaConstructorInvocation obj)
    {
        print(obj);
    }

    public void visit(JavaConstructorReference obj)
    {
        print(obj);
    }

    public void visit(JavaContinueStatement obj)
    {
        print(obj);
    }

    public void visit(JavaDivide obj)
    {
        print(obj);
    }

    public void visit(JavaDoubleLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaDoubleType obj)
    {
        print(obj);
    }

    public void visit(JavaDoWhileStatement obj)
    {
        print(obj);
    }

    public void visit(JavaEnum obj)
    {
        print((JavaSchemaMember) obj);
    }

    public void visit(JavaEqual obj)
    {
        print(obj);
    }

    public void visit(JavaExpressionStatement obj)
    {
        print(obj);
    }

    public void visit(JavaFloatLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaFloatType obj)
    {
        print(obj);
    }

    public void visit(JavaForStatement obj)
    {
        print(obj);
    }

    public void visit(JavaFunction obj)
    {
        print(obj);
    }

    public void visit(JavaFunctionArgument obj)
    {
        basePrinter.printVarDecl(obj, null);
    }

    public void visit(JavaFunctionInvocation obj)
    {
        print(obj);
    }

    public void visit(JavaFunctionReference obj)
    {
        print(obj);
    }

    public void visit(JavaFunctionType obj)
    {
        print(obj);
    }

    public void visit(JavaGreater obj)
    {
        print(obj);
    }

    public void visit(JavaGreaterOrEqual obj)
    {
        print(obj);
    }

    public void visit(JavaIfStatement obj)
    {
        print(obj);
    }

    public void visit(JavaInitializerBlock obj)
    {
        print((JavaClassMember) obj);
    }

    public void visit(JavaInstanceCreation obj)
    {
        print(obj);
    }

    public void visit(JavaInterface obj)
    {
        print((JavaSchemaMember) obj);
    }

    public void visit(JavaIntLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaIntType obj)
    {
        print(obj);
    }

    public void visit(JavaLabeledStatement obj)
    {
        print(obj);
    }

    public void visit(JavaLeftShift obj)
    {
        print(obj);
    }

    public void visit(JavaLess obj)
    {
        print(obj);
    }

    public void visit(JavaLessOrEqual obj)
    {
        print(obj);
    }

    public void visit(JavaLocalVariable obj)
    {
        print(obj);
    }

    public void visit(JavaLogicalNot obj)
    {
        print(obj);
    }

    public void visit(JavaLongLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaLongType obj)
    {
        print(obj);
    }

    public void visit(JavaMemberAccess obj)
    {
        print(obj);
    }

    public void visit(JavaMemberVariable obj)
    {
        print(obj);
    }

    public void visit(JavaModulo obj)
    {
        print(obj);
    }

    public void visit(JavaMultiply obj)
    {
        print(obj);
    }

    public void visit(JavaNotEqual obj)
    {
        print(obj);
    }

    public void visit(JavaNullLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaNullType obj)
    {
        print(obj);
    }

    public void visit(JavaOr obj)
    {
        print(obj);
    }

    public void visit(JavaPackage obj)
    {
        print(obj);
    }

    public void visit(JavaParameterizedType obj)
    {
        print((JavaType) obj);
    }

    public void visit(JavaPostDecrement obj)
    {
        print(obj);
    }

    public void visit(JavaPostIncrement obj)
    {
        print(obj);
    }

    public void visit(JavaPreDecrement obj)
    {
        print(obj);
    }

    public void visit(JavaPreIncrement obj)
    {
        print(obj);
    }

    public void visit(JavaReturnStatement obj)
    {
        print(obj);
    }

    public void visit(JavaShortType obj)
    {
        print(obj);
    }

    public void visit(JavaSignedRightShift obj)
    {
        print(obj);
    }

    public void visit(JavaStringLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaSubtract obj)
    {
        print(obj);
    }

    public void visit(JavaSuperReference obj)
    {
        print(obj);
    }

    public void visit(JavaSwitchStatement obj)
    {
        print(obj);
    }

    public void visit(JavaSynchronizedStatement obj)
    {
        print(obj);
    }

    public void visit(JavaThisReference obj)
    {
        print(obj);
    }

    public void visit(JavaThrowStatement obj)
    {
        print(obj);
    }

    public void visit(JavaTryStatement obj)
    {
        print(obj);
    }

    public void visit(JavaTypeLiteral obj)
    {
        print(obj);
    }

    public void visit(JavaTypeTest obj)
    {
        print(obj);
    }

    public void visit(JavaTypeVariable obj)
    {
        basePrinter.printTypeVariable(obj, null);
    }

    public void visit(JavaUnaryMinus obj)
    {
        print(obj);
    }

    public void visit(JavaUnaryPlus obj)
    {
        print(obj);
    }

    public void visit(JavaUnsignedRightShift obj)
    {
        print(obj);
    }

    public void visit(JavaVariableReference obj)
    {
        print(obj);
    }

    public void visit(JavaVoidType obj)
    {
        print(obj);
    }

    public void visit(JavaWhileStatement obj)
    {
        print(obj);
    }

    public void visit(JavaWildcardType obj)
    {
        print(obj);
    }

    public void visit(JavaXor obj)
    {
        print(obj);
    }
}
