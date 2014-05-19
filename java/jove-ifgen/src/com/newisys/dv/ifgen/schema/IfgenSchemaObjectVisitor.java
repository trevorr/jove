/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
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

package com.newisys.dv.ifgen.schema;

/**
 * Visitor over all Ifgen schema objects.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public interface IfgenSchemaObjectVisitor
    extends IfgenSchemaMemberVisitor, IfgenPackageMemberVisitor,
    IfgenInterfaceMemberVisitor, IfgenBindMemberVisitor
{
    void visit(IfgenBindSignal obj);

    void visit(IfgenDriveDef obj);

    void visit(IfgenEnum obj);

    void visit(IfgenEnumElement obj);

    void visit(IfgenEnumLiteral obj);

    void visit(IfgenIntegerLiteral obj);

    void visit(IfgenInterfaceSignal obj);

    void visit(IfgenModuleDef obj);

    void visit(IfgenComplexVariableRef obj);

    void visit(IfgenPortSignal obj);

    void visit(IfgenSampleDef obj);

    void visit(IfgenSetLiteral obj);

    void visit(IfgenSetOperator obj);

    void visit(IfgenStringLiteral obj);

    void visit(IfgenTaskArg obj);

    void visit(IfgenTestbench obj);

    void visit(IfgenTestbenchMember obj);

    void visit(IfgenVariableRef obj);

    void visit(IfgenVariableDecl obj);
}
