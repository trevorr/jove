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

/**
 * Visitor over Java types.
 * 
 * @author Trevor Robinson
 */
public interface JavaTypeVisitor
{
    void visit(JavaAnnotationType obj);

    void visit(JavaArrayType obj);

    void visit(JavaBooleanType obj);

    void visit(JavaByteType obj);

    void visit(JavaCharType obj);

    void visit(JavaClass obj);

    void visit(JavaDoubleType obj);

    void visit(JavaEnum obj);

    void visit(JavaFloatType obj);

    void visit(JavaFunctionType obj);

    void visit(JavaInterface obj);

    void visit(JavaIntType obj);

    void visit(JavaLongType obj);

    void visit(JavaNullType obj);

    void visit(JavaParameterizedType obj);

    void visit(JavaShortType obj);

    void visit(JavaTypeVariable obj);

    void visit(JavaVoidType obj);

    void visit(JavaWildcardType obj);
}
