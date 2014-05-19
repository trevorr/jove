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

package com.newisys.dv.ifgen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import com.newisys.dv.ClockSignal;
import com.newisys.dv.DV;
import com.newisys.dv.InOutSignal;
import com.newisys.dv.InputSignal;
import com.newisys.dv.OutputSignal;
import com.newisys.dv.PortSignalWrapper;
import com.newisys.dv.Signal;
import com.newisys.dv.ifgen.schema.*;
import com.newisys.langschema.BlockComment;
import com.newisys.langschema.Literal;
import com.newisys.langschema.java.*;
import com.newisys.langschema.java.util.ExpressionBuilder;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

/**
 * Translates Ifgen schema object to Java schema objects.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public class IfgenJavaTranslator
{
    private final JavaSchema javaSchema;

    private final JavaClass dvType;
    private final JavaVariableReference dvSimRef;

    private final String defClockAccessorName;

    private final JavaClass edgeSetType;

    private final JavaInterface signalType;
    private final JavaInterface clockSignalType;
    private final JavaInterface inputSignalType;
    private final JavaInterface outputSignalType;
    private final JavaInterface inOutSignalType;

    private final JavaClass portSignalWrapperType;

    private final JavaAnnotationType lengthType;

    private final JavaClass integerType;
    private final JavaInterface listType;
    private final JavaInterface mapType;
    private final JavaClass linkedListType;
    private final JavaClass hashMapType;
    private final JavaClass objectType;

    private final Map<IfgenDirection, JavaInterface> signalTypeMap;
    private final Map<JavaMemberVariable, JavaExpression> signalInitMap;

    private final Map<IfgenSchemaObject, JavaSchemaObject> xlatObjMap;
    private final Set<JavaRawStructuredType> xlatTypes;

    public IfgenJavaTranslator(JavaSchema javaSchema)
    {
        this.javaSchema = javaSchema;

        try
        {
            dvType = (JavaClass) javaSchema.getTypeForClass(DV.class.getName());
            dvSimRef = new JavaVariableReference(dvType.getField("simulation"));
            defClockAccessorName = "getDefaultClockSignal";

            edgeSetType = (JavaClass) javaSchema.getTypeForClass(EdgeSet.class
                .getName());

            signalType = (JavaInterface) javaSchema
                .getTypeForClass(Signal.class.getName());
            clockSignalType = (JavaInterface) javaSchema
                .getTypeForClass(ClockSignal.class.getName());
            inputSignalType = (JavaInterface) javaSchema
                .getTypeForClass(InputSignal.class.getName());
            outputSignalType = (JavaInterface) javaSchema
                .getTypeForClass(OutputSignal.class.getName());
            inOutSignalType = (JavaInterface) javaSchema
                .getTypeForClass(InOutSignal.class.getName());

            portSignalWrapperType = (JavaClass) javaSchema
                .getTypeForClass(PortSignalWrapper.class.getName());

            lengthType = (JavaAnnotationType) javaSchema
                .getTypeForClass(Length.class.getName());

            integerType = (JavaClass) javaSchema.getTypeForClass(Integer.class
                .getName());
            mapType = (JavaInterface) javaSchema.getTypeForClass(Map.class
                .getName());
            listType = (JavaInterface) javaSchema.getTypeForClass(List.class
                .getName());
            linkedListType = (JavaClass) javaSchema
                .getTypeForClass(LinkedList.class.getName());
            hashMapType = (JavaClass) javaSchema.getTypeForClass(HashMap.class
                .getName());
            objectType = (JavaClass) javaSchema.getTypeForClass(Object.class
                .getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new Error("Error loading type information: " + e, e);
        }

        signalTypeMap = new HashMap<IfgenDirection, JavaInterface>();
        signalTypeMap.put(IfgenDirection.INPUT, inputSignalType);
        signalTypeMap.put(IfgenDirection.OUTPUT, outputSignalType);
        signalTypeMap.put(IfgenDirection.INOUT, inOutSignalType);

        signalInitMap = new HashMap<JavaMemberVariable, JavaExpression>();
        xlatObjMap = new HashMap<IfgenSchemaObject, JavaSchemaObject>();
        xlatTypes = new LinkedHashSet<JavaRawStructuredType>();
    }

    public Map<IfgenSchemaObject, JavaSchemaObject> getXlatObjMap()
    {
        return xlatObjMap;
    }

    public Set<JavaRawStructuredType> getXlatTypes()
    {
        return xlatTypes;
    }

    public JavaPackage translatePackage(IfgenPackage pkg)
    {
        return (pkg != null) ? javaSchema.getPackage(pkg.getName()
            .getCanonicalName(), true) : null;
    }

    public JavaRawClass translateTestbench(IfgenTestbench tb)
        throws IfgenTranslatorException
    {
        JavaRawClass cls = (JavaRawClass) xlatObjMap.get(tb);
        if (cls != null) return cls;

        // get package
        IfgenPackage pkg = tb.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create interface class
        String tbID = tb.getName().getIdentifier();
        cls = new JavaRawClass(javaSchema, tbID, javaPkg);
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addModifier(JavaTypeModifier.FINAL);
        if (javaPkg != null)
        {
            javaPkg.addMember(cls);
        }
        else
        {
            javaSchema.addMember(cls);
        }
        xlatObjMap.put(tb, cls);
        xlatTypes.add(cls);

        cls.addAnnotation(new BlockComment("/ Generated testbench "
            + tb.getName()));

        final JavaConstructor defCtor = cls.newConstructor();
        defCtor.setVisibility(JavaVisibility.PRIVATE);
        defCtor.setBody(new JavaBlock(javaSchema));

        // List to keep the current variable declaration order
        List<IfgenVariableDecl> variableOrder = new LinkedList<IfgenVariableDecl>(
            tb.getParameters());

        translateTestbenchMembers(cls, variableOrder, tb);

        return cls;
    }

    private void translateTestbenchMembers(
        JavaRawClass tbClass,
        List<IfgenVariableDecl> varOrder,
        IfgenTestbenchMemberContainer container)
        throws IfgenTranslatorException
    {
        for (final IfgenTestbenchMember member : container.getMembers())
        {
            if (member instanceof IfgenForStatement)
            {
                IfgenForStatement forStmt = (IfgenForStatement) member;
                List<IfgenVariableDecl> newVarOrder = new LinkedList<IfgenVariableDecl>(
                    varOrder);
                newVarOrder.add(forStmt.getVarDecl());
                translateTestbenchMembers(tbClass, newVarOrder, forStmt);
            }
            else if (member instanceof IfgenTemplateInst)
            {
                IfgenTemplateInst tmplInst = (IfgenTemplateInst) member;
                if (tmplInst.getTemplate().getParameters().size() == 0)
                {
                    // non-templatized intf/bind/hdl_tasks don't go into the
                    // testbench
                    continue;
                }
                List<IfgenVariableDecl> args = getAccessorArguments(varOrder,
                    tmplInst.getArgs());

                switch (tmplInst.getTemplateKind())
                {
                case INTERFACE:
                    translateInterfaceAccessor(tbClass, args, tmplInst);
                    break;
                case BIND:
                    translateBindAccessor(tbClass, args, tmplInst);
                    break;
                case HDL_TASK:
                    translateHDLTaskAccessor(tbClass, args, tmplInst);
                    break;
                default:
                    throw new Error("Unknown template instantiation type: "
                        + tmplInst.getTemplateKind());
                }
            }
            else if (member instanceof IfgenInterface)
            {
                translateInterface((IfgenInterface) member);
            }
            else if (member instanceof IfgenBind)
            {
                translateBind((IfgenBind) member);
            }
            else if (member instanceof IfgenHDLTask)
            {
            	// do nothing for imported hdl tasks
            }
            else
            {
                assert (member instanceof IfgenHVLTask);
                // do nothing for imported hvl tasks
            }

        }
    }

    private void translateInterfaceAccessor(
        JavaRawClass tbClass,
        List<IfgenVariableDecl> args,
        IfgenTemplateInst intf)
    {
        System.out.println("Translating interface inst " + intf.getName());
        IfgenSchema ifgenSchema = intf.getSchema();
        JavaRawClass intfClass = translateParameterizedInterface((IfgenInterface) intf
            .getTemplate());

        // create the arguments to the method
        JavaFunction func = null;
        {
            List<JavaFunctionArgument> funcArgs = new LinkedList<JavaFunctionArgument>();
            for (final IfgenVariableDecl decl : args)
            {
                JavaType type = convertParamArgType(decl.getSchema(), decl
                    .getType());
                JavaFunctionArgument arg = new JavaFunctionArgument(decl
                    .getName().getIdentifier(), type);
                arg.addModifier(JavaVariableModifier.FINAL);
                funcArgs.add(arg);
            }

            JavaFunctionType funcType = new JavaFunctionType(intfClass,
                funcArgs);
            func = new JavaFunction("get" + intf.getName().getIdentifier(),
                funcType);
            func.setVisibility(JavaVisibility.PUBLIC);
            func.addModifier(JavaFunctionModifier.STATIC);
            tbClass.addMember(func);
        }

        // create the body of the accessor
        {
            JavaBlock body = new JavaBlock(javaSchema);
            JavaFunctionInvocation invoke = ExpressionBuilder.staticCall(
                intfClass, "get" + intfClass.getName().getIdentifier(),
                getJavaTemplateArguments(ifgenSchema, intf.getArgs()), tbClass);
            body.addMember(new JavaReturnStatement(javaSchema, invoke));
            func.setBody(body);
        }
    }

    private void translateBindAccessor(
        JavaRawClass tbClass,
        List<IfgenVariableDecl> args,
        IfgenTemplateInst bind)
    {
        System.out.println("Translating parameterized bind inst "
            + bind.getName());
        IfgenSchema ifgenSchema = bind.getSchema();
        IfgenBind bindTmpl = (IfgenBind) bind.getTemplate();
        JavaRawClass portClass = translatePort(bindTmpl.getPort());
        boolean bidirPort = bindTmpl.getPort().getReverseOf() == null;
        final String cacheName = "cache" + bind.getName().getIdentifier();

        // create a cache in the testbench for ports of this bind
        JavaParameterizedInterface listOfWildCards = (JavaParameterizedInterface) listType
            .parameterize(new JavaWildcardType(javaSchema));
        JavaParameterizedInterface cacheIntf = (JavaParameterizedInterface) mapType
            .parameterize(listOfWildCards, portClass);
        JavaParameterizedClass cacheClass = (JavaParameterizedClass) hashMapType
            .parameterize(listOfWildCards, portClass);
        JavaParameterizedClass listOfObjects = (JavaParameterizedClass) linkedListType
            .parameterize(objectType);
        // caches are declarations like:
        // private static final Map<List<Object>, [PORT_TYPE]> [BIND_NAME]cache =
        //              new HashMap<List<Object>, [PORT_TYPE]>();
        JavaMemberVariable cache = new JavaMemberVariable(cacheName, cacheIntf);
        cache.setVisibility(JavaVisibility.PRIVATE);
        cache.addModifier(JavaVariableModifier.TRANSIENT);
        cache.addModifier(JavaVariableModifier.STATIC);
        cache.addModifier(JavaVariableModifier.FINAL);
        cache.setInitializer(ExpressionBuilder.newInstance(cacheClass));
        JavaClassMember firstNonMember = null;
        {
            List<JavaClassMember> members = tbClass.getMembers();
            for (final JavaClassMember m : members)
            {
                if (!(m instanceof JavaMemberVariable))
                {
                    firstNonMember = m;
                    break;
                }
            }
        }
        if (firstNonMember == null)
        {
            tbClass.addMember(cache);
        }
        else
        {
            tbClass.addMemberBefore(cache, firstNonMember);
        }

        // create the arguments to the method
        JavaFunction func = null;
        List<JavaFunctionArgument> funcArgs = new LinkedList<JavaFunctionArgument>();
        {
            for (final IfgenVariableDecl decl : bind.getTemplate()
                .getParameters())
            {
                JavaType type = convertParamArgType(decl.getSchema(), decl
                    .getType());
                JavaFunctionArgument arg = new JavaFunctionArgument(decl
                    .getName().getIdentifier(), type);
                arg.addModifier(JavaVariableModifier.FINAL);
                funcArgs.add(arg);
            }

            JavaFunctionType funcType = new JavaFunctionType(portClass,
                funcArgs);
            func = new JavaFunction("get" + bind.getName().getIdentifier(),
                funcType);
            func.setVisibility(JavaVisibility.PUBLIC);
            func.addModifier(JavaFunctionModifier.STATIC);
            tbClass.addMember(func);
        }

        // create the method body
        {
            int intfNum = 0;
            JavaBlock body = new JavaBlock(javaSchema);
            func.setBody(body);

            // List<Object> args = new LinkedList<Object>();
            // args.add(arg1); args.add(arg2); ...
            // PEPort port = cache[BIND_NAME].get(args);

            JavaLocalVariable argList = new JavaLocalVariable("args",
                listOfObjects);
            argList
                .setInitializer(ExpressionBuilder.newInstance(listOfObjects));
            argList.addModifier(JavaVariableModifier.FINAL);
            body.addMember(argList);
            for (final JavaFunctionArgument arg : funcArgs)
            {
                // args.add(argN);
                body.addMember(new JavaExpressionStatement(ExpressionBuilder
                    .memberCall(new JavaVariableReference(argList), "add",
                        new JavaVariableReference(arg))));
            }

            // PEPort = cache[BIND_NAME].get(args);
            JavaLocalVariable portVar = new JavaLocalVariable("port", portClass);
            portVar.setInitializer(ExpressionBuilder.memberCall(
                new JavaVariableReference(cache), "get",
                new JavaVariableReference(argList)));
            body.addMember(portVar);
            JavaVariableReference portRef = new JavaVariableReference(portVar);

            // create code for if(port == null) { ... }
            {
                JavaExpression portIsNull = new JavaEqual(javaSchema,
                    new JavaVariableReference(portVar), new JavaNullLiteral(
                        javaSchema));
                JavaBlock ifBody = new JavaBlock(javaSchema);
                JavaIfStatement ifStmt = new JavaIfStatement(portIsNull, ifBody);
                body.addMember(ifStmt);

                // port = new [PORT_NAME]();
                ifBody.addMember(new JavaExpressionStatement(new JavaAssign(
                    javaSchema, portRef, ExpressionBuilder
                        .newInstance(portClass))));

                IfgenInterface intf = null;
                JavaClass intfClass = null;
                JavaVariableReference intfRef = null;
                for (final IfgenBindMember member : bindTmpl.getMembers())
                {
                    if (member instanceof IfgenInterfaceDef)
                    {
                        // final <INTF> intf = <INTF>.get<INTF>(arg1, arg2, ...);
                        IfgenInterfaceDef intfDef = (IfgenInterfaceDef) member;
                        intf = intfDef.getIntf();
                        if (intf.hasParameters())
                        {
                            intfClass = translateParameterizedInterface(intf);
                            JavaFunctionInvocation invoke = ExpressionBuilder
                                .staticCall(intfClass, "get"
                                    + intfClass.getName().getIdentifier(),
                                    getJavaTemplateArguments(ifgenSchema,
                                        intfDef.getArgs()), tbClass);
                            JavaLocalVariable intfVar = new JavaLocalVariable(
                                "intf" + (intfNum++), intfClass);
                            intfVar.setInitializer(invoke);
                            intfVar.addModifier(JavaVariableModifier.FINAL);
                            intfRef = new JavaVariableReference(intfVar);
                            ifBody.addMember(intfVar);
                        }
                    }
                    else
                    {
                        assert (member instanceof IfgenBindSignal);
                        IfgenBindSignal bindSignal = (IfgenBindSignal) member;
                        IfgenPortSignal portSignal = bindSignal.getPortSignal();
                        JavaMemberVariable portSignalVar = (JavaMemberVariable) xlatObjMap
                            .get(portSignal);
                        assert (portSignalVar != null);
                        JavaMemberAccess portSignalExpr = new JavaMemberAccess(
                            portRef, portSignalVar);
                        IfgenSignalRef intfSignal = bindSignal.getIntfSignal();

                        JavaExpression intfSignalExpr = translateSignalRef(intfSignal);
                        if (intf.hasParameters())
                        {
                            // find all JavaVariableReferences in the expression and
                            // make them member accesses from the interface
                            intfSignalExpr = makeMemberAccess(intfSignalExpr,
                                intfRef);
                        }
                        JavaType exprType = intfSignalExpr.getResultType();
                        // create runtime-checked wrapper for unidirectional signals
                        if (!portSignalVar.getType().isAssignableFrom(exprType))
                        {
                            if (bidirPort)
                            {
                                intfSignalExpr = ExpressionBuilder.newInstance(
                                    portSignalWrapperType, intfSignalExpr);
                            }
                            else
                            {
                                IfgenSignalType signalType = getSignalType(exprType);
                                throw new Error("Error in bind "
                                    + bind.getName().getIdentifier()
                                    + ": Direction ("
                                    + portSignal.getDirection()
                                    + ") of port signal '"
                                    + portSignal.getName()
                                    + "' does not match type (" + signalType
                                    + ") of interface signal '" + intfSignal
                                    + "'");
                            }
                        }

                        JavaAssign assign = new JavaAssign(javaSchema,
                            portSignalExpr, intfSignalExpr);
                        ifBody.addMember(new JavaExpressionStatement(assign));
                    }
                }
                ifBody.addMember(new JavaExpressionStatement(ExpressionBuilder
                    .memberCall(new JavaVariableReference(cache), "put",
                        new JavaVariableReference(argList), portRef)));
                body.addMember(new JavaReturnStatement(javaSchema, portRef));
            }
        }
    }

    private void translateHDLTaskAccessor(
    	JavaRawClass tbClass,
        List<IfgenVariableDecl> args,
        IfgenTemplateInst task)
    {
        System.out.println("Translating task inst " + task.getName());
        IfgenSchema ifgenSchema = task.getSchema();
        JavaRawClass taskClass = translateParameterizedHDLTask((IfgenHDLTask) task
            .getTemplate());

        // create the arguments to the method
        JavaFunction func = null;
        {
            List<JavaFunctionArgument> funcArgs = new LinkedList<JavaFunctionArgument>();
            for (final IfgenVariableDecl decl : args)
            {
                JavaType type = convertParamArgType(decl.getSchema(), decl
                    .getType());
                JavaFunctionArgument arg = new JavaFunctionArgument(decl
                    .getName().getIdentifier(), type);
                arg.addModifier(JavaVariableModifier.FINAL);
                funcArgs.add(arg);
            }

            JavaFunctionType funcType = new JavaFunctionType(taskClass,
                funcArgs);
            func = new JavaFunction("get" + task.getName().getIdentifier(),
                funcType);
            func.setVisibility(JavaVisibility.PUBLIC);
            func.addModifier(JavaFunctionModifier.STATIC);
            tbClass.addMember(func);
        }

        // create the body of the accessor
        {
            JavaBlock body = new JavaBlock(javaSchema);
            JavaFunctionInvocation invoke = ExpressionBuilder.staticCall(
                taskClass, "get" + taskClass.getName().getIdentifier(),
                getJavaTemplateArguments(ifgenSchema, task.getArgs()), tbClass);
            body.addMember(new JavaReturnStatement(javaSchema, invoke));
            func.setBody(body);
        }
    }
   
    private JavaExpression[] getJavaTemplateArguments(
        IfgenSchema ifgenSchema,
        List<IfgenExpression> expressions)
    {
        final JavaExpression[] funcArgs = new JavaExpression[expressions.size()];
        int argIdx = 0;
        for (final IfgenExpression expr : expressions)
        {
            IfgenType ifgenType = expr.getType();
            JavaType type = convertParamArgType(expr.getSchema(), expr
                .getType());
            if (ifgenType == ifgenSchema.INTEGER_TYPE)
            {
                if (expr instanceof IfgenIntegerLiteral)
                {
                    funcArgs[argIdx++] = new JavaIntLiteral(javaSchema,
                        ((IfgenIntegerLiteral) expr).getValue());
                }
                else
                {
                    assert (expr instanceof IfgenVariableRef);
                    funcArgs[argIdx++] = new JavaVariableReference(
                        new JavaFunctionArgument(((IfgenVariableRef) expr)
                            .getName().getIdentifier(), type));
                }
            }
            else if (ifgenType == ifgenSchema.STRING_TYPE)
            {
                if (expr instanceof IfgenStringLiteral)
                {
                    funcArgs[argIdx++] = new JavaStringLiteral(javaSchema,
                        ((IfgenStringLiteral) expr).getString());
                }
                else if (expr instanceof IfgenVariableRef)
                {
                    funcArgs[argIdx++] = new JavaVariableReference(
                        new JavaFunctionArgument(((IfgenVariableRef) expr)
                            .getName().getIdentifier(), type));
                }
                else
                {
                    assert (expr instanceof IfgenComplexVariableRef);
                    Iterator<IfgenExpression> iter = ((IfgenComplexVariableRef) expr)
                        .getExpressions().iterator();
                    JavaExpression concatExpr = null;
                    while (iter.hasNext())
                    {
                        final IfgenExpression chunk = iter.next();
                        final JavaExpression chunkExpr;
                        if (chunk instanceof IfgenStringLiteral)
                        {
                            chunkExpr = new JavaStringLiteral(javaSchema,
                                ((IfgenStringLiteral) chunk).getString());
                        }
                        else
                        {
                            assert (chunk instanceof IfgenVariableRef);
                            IfgenVariableDecl decl = ((IfgenVariableRef) chunk)
                                .getDecl();

                            chunkExpr = new JavaVariableReference(
                                new JavaFunctionArgument(decl.getName()
                                    .getIdentifier(), type));
                        }

                        assert (chunkExpr != null);
                        if (concatExpr == null)
                        {
                            concatExpr = chunkExpr;
                        }
                        else
                        {
                            concatExpr = new JavaAdd(javaSchema, concatExpr,
                                chunkExpr);
                        }
                    }
                    funcArgs[argIdx++] = concatExpr;
                }
            }
            else
            {
                assert (ifgenType instanceof IfgenEnumType);
                assert (type instanceof JavaEnum);
                if (expr instanceof IfgenEnumLiteral)
                {
                    JavaVariable elt = ((JavaEnum) type)
                        .getField(((IfgenEnumLiteral) expr).getElement()
                            .getName().getIdentifier());
                    funcArgs[argIdx++] = new JavaVariableReference(elt);
                }
                else
                {
                    assert (expr instanceof IfgenVariableRef);
                    funcArgs[argIdx++] = new JavaVariableReference(
                        new JavaFunctionArgument(((IfgenVariableRef) expr)
                            .getName().getIdentifier(), type));
                }
            }
        }

        return funcArgs;
    }

    private List<IfgenVariableDecl> getAccessorArguments(
        List<IfgenVariableDecl> varOrder,
        List<IfgenExpression> varExprs)
    {
        // Find all variable refs used in varExprs and create a list of them
        // ordered as they're ordered in varOrder.

        List<IfgenVariableDecl> refDecls = new LinkedList<IfgenVariableDecl>();
        for (final IfgenExpression expr : varExprs)
        {
            if (expr instanceof IfgenComplexVariableRef)
            {
                IfgenComplexVariableRef cRef = (IfgenComplexVariableRef) expr;
                for (final IfgenVariableRef ref : cRef.getVariableRefs())
                {
                    assert (!refDecls.contains(ref.getDecl()));
                    refDecls.add(ref.getDecl());
                }
            }
            else if (expr instanceof IfgenVariableRef)
            {
                IfgenVariableRef ref = (IfgenVariableRef) expr;
                refDecls.add(ref.getDecl());
            }
        }

        List<IfgenVariableDecl> orderedArgs = new LinkedList<IfgenVariableDecl>();
        for (final IfgenVariableDecl decl : varOrder)
        {
            if (refDecls.contains(decl))
            {
                orderedArgs.add(decl);
            }
        }

        return orderedArgs;
    }

    public JavaEnum translateEnum(IfgenEnum enumeration)
    {
        JavaEnum e = (JavaEnum) xlatObjMap.get(enumeration);
        if (e != null) return e;

        // get package
        IfgenPackage pkg = enumeration.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create bind class
        IfgenName enumName = enumeration.getName();
        String enumID = enumName.getIdentifier();
        JavaEnum cls = new JavaEnum(javaSchema, enumID, javaPkg);
        cls.setVisibility(JavaVisibility.PUBLIC);
        if (javaPkg != null)
            javaPkg.addMember(cls);
        else
            javaSchema.addMember(cls);
        xlatTypes.add(cls);

        cls.addAnnotation(new BlockComment("/ Generated enum "
            + enumeration.getName()));

        // add enum elements
        for (final IfgenEnumElement elt : enumeration.getElements())
        {
            cls.newValue(elt.getName().getIdentifier(), false);
        }
        xlatObjMap.put(enumeration, e);

        return e;
    }

    public JavaMemberVariable translateBind(IfgenBind bind)
        throws IfgenTranslatorException
    {
        if (bind.hasParameters())
        {
            return null;
        }

        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap.get(bind);
        if (var != null) return var;

        // get package
        IfgenPackage pkg = bind.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create bind class
        IfgenName bindName = bind.getName();
        String bindID = bindName.getIdentifier();
        JavaRawClass cls = new JavaRawClass(javaSchema, bindID, javaPkg);
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addModifier(JavaTypeModifier.FINAL);
        if (javaPkg != null)
            javaPkg.addMember(cls);
        else
            javaSchema.addMember(cls);
        xlatTypes.add(cls);

        // get port
        IfgenPort port = bind.getPort();
        JavaClass portCls = translatePort(port);
        boolean bidirPort = port.getReverseOf() == null;

        cls.addAnnotation(new BlockComment("/ Generated bind to port "
            + port.getName()));

        // create instance variable
        var = cls.newField("INSTANCE", portCls);
        var.setVisibility(JavaVisibility.PUBLIC);
        var.addModifier(JavaVariableModifier.STATIC);
        var.addModifier(JavaVariableModifier.FINAL);
        var.setInitializer(ExpressionBuilder.newInstance(portCls));
        xlatObjMap.put(bind, var);

        // create initializer block for bind
        JavaVariableReference varRef = new JavaVariableReference(var);
        JavaInitializerBlock initBlock = new JavaInitializerBlock(javaSchema,
            true);
        for (IfgenBindMember member : bind.getMembers())
        {
            if (member instanceof IfgenBindSignal)
            {
                IfgenBindSignal bindSignal = (IfgenBindSignal) member;

                IfgenPortSignal portSignal = bindSignal.getPortSignal();
                JavaMemberVariable portSignalVar = (JavaMemberVariable) xlatObjMap
                    .get(portSignal);
                assert (portSignalVar != null);
                JavaMemberAccess portSignalExpr = new JavaMemberAccess(varRef,
                    portSignalVar);

                IfgenSignalRef intfSignal = bindSignal.getIntfSignal();
                JavaExpression intfSignalExpr = translateSignalRef(intfSignal);
                JavaType exprType = intfSignalExpr.getResultType();

                // create runtime-checked wrapper for unidirectional signals
                if (!portSignalVar.getType().isAssignableFrom(exprType))
                {
                    if (bidirPort)
                    {
                        intfSignalExpr = ExpressionBuilder.newInstance(
                            portSignalWrapperType, intfSignalExpr);
                    }
                    else
                    {
                        IfgenSignalType signalType = getSignalType(exprType);
                        throw new IfgenTranslatorException("Error in bind "
                            + bindName + ": Direction ("
                            + portSignal.getDirection() + ") of port signal '"
                            + portSignal.getName() + "' does not match type ("
                            + signalType + ") of interface signal '"
                            + intfSignal + "'");
                    }
                }

                JavaAssign assign = new JavaAssign(javaSchema, portSignalExpr,
                    intfSignalExpr);
                initBlock.addMember(new JavaExpressionStatement(assign));
            }
        }
        cls.addMember(initBlock);

        // create empty private ctor to prevent instantiation
        JavaConstructor ctor = cls.newConstructor();
        ctor.setVisibility(JavaVisibility.PRIVATE);
        ctor.setBody(new JavaBlock(javaSchema));

        return var;
    }

    public JavaExpression translateSignalRef(IfgenSignalRef ref)
    {
        class RefXlatVisitor
            implements IfgenSignalRefVisitor
        {
            public JavaExpression result;

            public void visit(IfgenConcatSignalRef obj)
            {
                result = translateSignalRef(obj);
            }

            public void visit(IfgenHDLSignalRef obj)
            {
                throw new UnsupportedOperationException(
                    "HDL signal reference in bind");
            }

            public void visit(IfgenInterfaceSignalRef obj)
            {
                result = translateSignalRef(obj);
            }

            public void visit(IfgenSliceSignalRef obj)
            {
                result = translateSignalRef(obj);
            }
        };

        RefXlatVisitor visitor = new RefXlatVisitor();
        ref.accept(visitor);
        return visitor.result;
    }

    public JavaExpression translateSignalRef(IfgenInterfaceSignalRef ref)
    {
        IfgenInterfaceSignal signal = ref.getSignal();
        JavaMemberVariable signalVar = translateInterfaceSignal(signal);
        return new JavaVariableReference(signalVar);
    }

    public JavaExpression translateSignalRef(IfgenSliceSignalRef ref)
    {
        final IfgenSignalRef baseRef = ref.getSignal();
        final JavaExpression baseExpr = translateSignalRef(baseRef);
        final JavaIntLiteral fromBitExpr = new JavaIntLiteral(javaSchema, ref
            .getFromIndex());
        final JavaIntLiteral toBitExpr = new JavaIntLiteral(javaSchema, ref
            .getToIndex());
        final JavaType exprType = baseExpr.getResultType();
        final IfgenSignalType signalType = getSignalType(exprType);
        final String typeName = getSignalTypeName(signalType);
        final String methodName = "getPartial" + typeName + "Signal";
        return ExpressionBuilder.memberCall(dvSimRef, methodName,
            new JavaExpression[] { baseExpr, fromBitExpr, toBitExpr }, null);
    }

    public JavaExpression translateSignalRef(IfgenConcatSignalRef ref)
    {
        JavaType elemType = null;
        JavaArrayType arrayType = null;
        JavaArrayInitializer argsInitExpr = null;
        for (IfgenSignalRef member : ref.getMembers())
        {
            JavaExpression memberExpr = translateSignalRef(member);
            if (argsInitExpr == null)
            {
                // create array initializer on first element, using its
                // result type for the array type
                elemType = memberExpr.getResultType();
                arrayType = javaSchema.getArrayType(elemType, 1);
                argsInitExpr = new JavaArrayInitializer(arrayType);
            }
            argsInitExpr.addElement(memberExpr);
        }
        final JavaArrayCreation argsNewExpr = new JavaArrayCreation(arrayType);
        argsNewExpr.setInitializer(argsInitExpr);
        final IfgenSignalType signalType = getSignalType(elemType);
        final String typeName = getSignalTypeName(signalType);
        final String methodName = "getConcat" + typeName + "Signal";
        return ExpressionBuilder.memberCall(dvSimRef, methodName, argsNewExpr);
    }

    private IfgenSignalType getSignalType(JavaType type)
    {
        if (clockSignalType.isAssignableFrom(type))
            return IfgenSignalType.CLOCK;
        if (inOutSignalType.isAssignableFrom(type))
            return IfgenSignalType.INOUT;
        if (outputSignalType.isAssignableFrom(type))
            return IfgenSignalType.OUTPUT;
        if (inputSignalType.isAssignableFrom(type))
            return IfgenSignalType.INPUT;
        throw new AssertionError("Unknown signal type: " + type);
    }

    // converts an ifgen schema type to the corresponding java schema type
    private JavaType convertParamArgType(IfgenSchema schema, IfgenType type)
    {
        // force Bit -> BitVector conversion
        return convertParamArgType(schema, type, false);
    }

    private JavaType convertParamArgType(
        IfgenSchema schema,
        IfgenType type,
        boolean allowBitType)
    {
        if (type == schema.INTEGER_TYPE)
            return javaSchema.integerWrapperType;
        else if (type == schema.STRING_TYPE)
            return javaSchema.getStringType();
        else
        {
            final String classQname;
            if (type == schema.BIT_TYPE)
            {
                if (allowBitType)
                {
                    classQname = Bit.class.getName();
                }
                else
                {
                    classQname = BitVector.class.getName();
                }
            }
            else
            {
                if (!(type instanceof IfgenEnumType))
                {
                    throw new AssertionError("Unknown argument type: " + type);
                }
                IfgenEnumType eType = (IfgenEnumType) type;
                classQname = eType.getName().toString();
            }
            try
            {
                return javaSchema.getTypeForClass(classQname);
            }
            catch (ClassNotFoundException e)
            {
                throw new Error(e);
            }
        }
    }

    private String getSignalTypeName(IfgenSignalType type)
    {
        switch (type)
        {
        case CLOCK:
        case INPUT:
            return "Input";
        case OUTPUT:
            return "Output";
        case INOUT:
            return "InOut";
        default:
            throw new AssertionError("Unknown signal type: " + type);
        }
    }

    private JavaRawClass getTasksClass(IfgenPackage pkg)
    {
        JavaPackage javaPkg = translatePackage(pkg);

        // All HDL tasks for package com.foo.bar will be placed in
        // a class called com.foo.bar.BarTasks
        final String pkgName = javaPkg.getName().getCanonicalName();
        final int lastDot = pkgName.lastIndexOf(".");
        final String oldChar = String.valueOf(pkgName.charAt(lastDot + 1));
        final String className = pkgName.substring(lastDot + 1).replaceFirst(
            oldChar, oldChar.toUpperCase())
            + "Tasks";

        // Check if this class has already been generated
        JavaRawClass cls = null;
        for (final JavaRawStructuredType type : xlatTypes)
        {
            if (type instanceof JavaRawClass)
            {
                JavaRawClass rawClass = (JavaRawClass) type;
                if (rawClass.getName().getCanonicalName().equals(
                    pkgName + "." + className))
                {
                    cls = rawClass;
                    break;
                }
            }
        }

        if (cls == null)
        {
            // create bind class
            cls = new JavaRawClass(javaSchema, className, javaPkg);
            cls.setVisibility(JavaVisibility.PUBLIC);
            cls.addModifier(JavaTypeModifier.FINAL);
            if (javaPkg != null)
                javaPkg.addMember(cls);
            else
                javaSchema.addMember(cls);
            xlatTypes.add(cls);

            cls.addAnnotation(new BlockComment(
                "/ Generated task wrappers for HDL Tasks in "
                    + pkg.getName().getCanonicalName()));

            final JavaConstructor defCtor = cls.newConstructor();
            defCtor.setVisibility(JavaVisibility.PRIVATE);
            defCtor.setBody(new JavaBlock(javaSchema));
        }

        return cls;
    }

    public JavaRawClass translateInterface(IfgenInterface intf)
    {
        return (intf.hasParameters()) ? translateParameterizedInterface(intf)
            : translateNonParameterizedInterface(intf);
    }

    private JavaRawClass translateParameterizedInterface(IfgenInterface intf)
    {
        assert (intf.hasParameters());
        JavaRawClass cls = (JavaRawClass) xlatObjMap.get(intf);
        if (cls != null) return cls;

        // get package
        IfgenPackage pkg = intf.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create interface class
        String intfID = intf.getName().getIdentifier();
        cls = new JavaRawClass(javaSchema, intfID, javaPkg);
        cls.setVisibility(JavaVisibility.DEFAULT);
        cls.addModifier(JavaTypeModifier.FINAL);
        if (javaPkg != null)
            javaPkg.addMember(cls);
        else
            javaSchema.addMember(cls);
        xlatObjMap.put(intf, cls);
        xlatTypes.add(cls);

        cls.addAnnotation(new BlockComment(
            "/ Generated parameterized interface"));

        // Create a List of arguments to this interface
        List<JavaFunctionArgument> templateArgs = new LinkedList<JavaFunctionArgument>();
        JavaExpression intfNameExpr = null;
        boolean stringInName = false;
        for (final IfgenVariableDecl decl : intf.getParameters())
        {
            JavaType type = convertParamArgType(decl.getSchema(), decl
                .getType());
            JavaFunctionArgument arg = new JavaFunctionArgument(decl.getName()
                .getIdentifier(), type);
            arg.addModifier(JavaVariableModifier.FINAL);
            templateArgs.add(arg);
            stringInName |= type == javaSchema.getStringType();
        }

        // we can infer that there's a string in the name if the num of params
        // is greater than 1 since we insert a "_" in between each parameter
        stringInName |= intf.getParameters().size() > 1;

        for (final JavaFunctionArgument arg : templateArgs)
        {
            JavaExpression curExpr = new JavaVariableReference(arg);
            if (!stringInName && arg.getType() == javaSchema.intType)
            {
                curExpr = ExpressionBuilder.staticCall(integerType, "toString",
                    curExpr);
            }
            else if (!stringInName)
            {
                curExpr = ExpressionBuilder.memberCall(curExpr, "toString");
            }

            if (intfNameExpr == null)
            {
                intfNameExpr = curExpr;
            }
            else
            {
                intfNameExpr = new JavaAdd(javaSchema, intfNameExpr,
                    new JavaAdd(javaSchema, new JavaStringLiteral(javaSchema,
                        "_"), curExpr));
            }
        }
        JavaLocalVariable intfNameExt = new JavaLocalVariable("intfName",
            javaSchema.getStringType());
        intfNameExt.setInitializer(intfNameExpr);
        JavaVariableReference intfNameExtRef = new JavaVariableReference(
            intfNameExt);

        // translate clock signal (if any)
        JavaExpression clockRef = null;
        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenInterfaceSignal)
            {
                IfgenInterfaceSignal signal = (IfgenInterfaceSignal) member;
                if (signal.getType() == IfgenSignalType.CLOCK)
                {
                    clockRef = new JavaVariableReference(
                        translateInterfaceSignal(cls, intf, signal,
                            intfNameExtRef, false, null));
                    break;
                }
            }
        }
        if (clockRef == null)
        {
            System.out
                .println("Warning: Using DV.simulation.getDefaultClock() for interface "
                    + intf.getName());
            clockRef = ExpressionBuilder.memberCall(dvSimRef,
                defClockAccessorName);
        }
        assert (clockRef != null);

        // translate non-clock signals
        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenInterfaceSignal)
            {
                IfgenInterfaceSignal signal = (IfgenInterfaceSignal) member;
                if (signal.getType() != IfgenSignalType.CLOCK)
                {
                    translateInterfaceSignal(cls, intf, signal, intfNameExtRef,
                        false, clockRef);
                }
            }
        }

        // create a Map to cache instances
        JavaParameterizedInterface listOfWildCards = (JavaParameterizedInterface) listType
            .parameterize(new JavaWildcardType(javaSchema));
        JavaParameterizedInterface cacheIntf = (JavaParameterizedInterface) mapType
            .parameterize(listOfWildCards, cls);
        JavaParameterizedClass cacheClass = (JavaParameterizedClass) hashMapType
            .parameterize(listOfWildCards, cls);
        JavaParameterizedClass listOfObjects = (JavaParameterizedClass) linkedListType
            .parameterize(objectType);

        JavaMemberVariable cache = new JavaMemberVariable("cache", cacheIntf);
        cache.setVisibility(JavaVisibility.PRIVATE);
        cache.addModifier(JavaVariableModifier.TRANSIENT);
        cache.addModifier(JavaVariableModifier.FINAL);
        cache.addModifier(JavaVariableModifier.STATIC);
        cache.setInitializer(ExpressionBuilder.newInstance(cacheClass));
        cls.addMember(cache);

        // create private ctor to prevent instantiation that assigns all fields (i.e. this.x = x)
        List<JavaMemberVariable> signalMembers = new LinkedList<JavaMemberVariable>();
        {
            List<JavaClassMember> members = cls.getMembers();
            List<JavaFunctionArgument> ctorArgs = new LinkedList<JavaFunctionArgument>();
            for (final JavaClassMember member : members)
            {
                if (member instanceof JavaMemberVariable)
                {
                    JavaMemberVariable memberVar = (JavaMemberVariable) member;
                    if (signalType.isAssignableFrom(memberVar.getType()))
                    {
                        signalMembers.add(memberVar);
                        JavaFunctionArgument ctorArg = new JavaFunctionArgument(
                            memberVar.getName().getIdentifier(), memberVar
                                .getType());
                        ctorArg.addModifier(JavaVariableModifier.FINAL);
                        ctorArgs.add(ctorArg);
                    }
                }
            }
            JavaConstructor ctor = new JavaConstructor(new JavaFunctionType(
                javaSchema.voidType, ctorArgs));
            JavaBlock body = new JavaBlock(javaSchema);
            ctor.setVisibility(JavaVisibility.PRIVATE);
            ctor.setBody(body);
            cls.addMember(ctor);

            // assign each member from its respective argument
            {
                for (final JavaFunctionArgument arg : ctorArgs)
                {
                    JavaExpression lhs = new JavaMemberAccess(
                        new JavaThisReference(cls), cls.getField(arg.getName()
                            .getIdentifier()));
                    JavaExpression rhs = new JavaVariableReference(arg);
                    body.addMember(new JavaExpressionStatement(new JavaAssign(
                        javaSchema, lhs, rhs)));
                }
            }
        }

        // create static accessor
        {
            JavaFunctionType funcType = new JavaFunctionType(cls, templateArgs);
            JavaFunction func = new JavaFunction("get"
                + intf.getName().getIdentifier(), funcType);
            JavaBlock body = new JavaBlock(javaSchema);
            func.setBody(body);
            func.setVisibility(JavaVisibility.DEFAULT);
            func.addModifier(JavaFunctionModifier.STATIC);

            // Create accessor body
            {
                // LinkedList<Object> args = new LinkedList<Object>();
                JavaLocalVariable argList = new JavaLocalVariable("args",
                    listOfObjects);
                argList.setInitializer(ExpressionBuilder
                    .newInstance(listOfObjects));
                argList.addModifier(JavaVariableModifier.FINAL);
                body.addMember(argList);
                for (final JavaFunctionArgument arg : templateArgs)
                {
                    // args.add(argN);
                    body.addMember(new JavaExpressionStatement(
                        ExpressionBuilder.memberCall(new JavaVariableReference(
                            argList), "add", new JavaVariableReference(arg))));
                }

                // tmpl = cache.get(args);
                JavaLocalVariable intfInst = new JavaLocalVariable("tmpl", cls);
                intfInst.setInitializer(ExpressionBuilder.memberCall(
                    new JavaVariableReference(cache), "get",
                    new JavaVariableReference(argList)));
                body.addMember(intfInst);

                // create code for if(tmpl == null) { ... }
                {
                    JavaExpression intfInstIsNull = new JavaEqual(javaSchema,
                        new JavaVariableReference(intfInst),
                        new JavaNullLiteral(javaSchema));
                    JavaBlock ifBody = new JavaBlock(javaSchema);
                    JavaIfStatement ifStmt = new JavaIfStatement(
                        intfInstIsNull, ifBody);
                    body.addMember(ifStmt);

                    ifBody.addMember(intfNameExt);
                    JavaExpression[] ctorArgs = new JavaExpression[signalMembers
                        .size()];
                    int ctorArgIdx = 0;
                    for (final JavaMemberVariable member : signalMembers)
                    {
                        // Signal signal1 = DV.simulation...();
                        JavaLocalVariable var = new JavaLocalVariable(member
                            .getName().getIdentifier(), member.getType());
                        JavaExpression varInitExpr = signalInitMap.get(member);
                        assert (varInitExpr != null);
                        var.setInitializer(varInitExpr);
                        var.addModifier(JavaVariableModifier.FINAL);
                        ctorArgs[ctorArgIdx++] = new JavaVariableReference(var);
                        ifBody.addMember(var);
                    }

                    // tmpl = new <CLASS>(Signal1, Signal2, ...);
                    ifBody.addMember(new JavaExpressionStatement(
                        new JavaAssign(javaSchema, new JavaVariableReference(
                            intfInst), ExpressionBuilder.newInstance(cls,
                            ctorArgs, cls))));

                    // cache.put(args, tmpl);
                    ifBody.addMember(new JavaExpressionStatement(
                        ExpressionBuilder.memberCall(new JavaVariableReference(
                            cache), "put", new JavaVariableReference(argList),
                            new JavaVariableReference(intfInst))));
                }

                body.addMember(new JavaReturnStatement(javaSchema,
                    new JavaVariableReference(intfInst)));
            }
            cls.addMember(func);

        }

        return cls;
    }

    private JavaRawClass translateNonParameterizedInterface(IfgenInterface intf)
    {
        assert (!intf.hasParameters());
        JavaRawClass cls = (JavaRawClass) xlatObjMap.get(intf);
        if (cls != null) return cls;

        // get package
        IfgenPackage pkg = intf.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create interface class
        String intfID = intf.getName().getIdentifier();
        cls = new JavaRawClass(javaSchema, intfID, javaPkg);
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addModifier(JavaTypeModifier.FINAL);

        if (javaPkg != null)
            javaPkg.addMember(cls);
        else
            javaSchema.addMember(cls);
        xlatObjMap.put(intf, cls);
        xlatTypes.add(cls);

        cls.addAnnotation(new BlockComment(
            "/ Generated non-parameterized interface"));

        // translate clock signal (if any)
        JavaExpression clockRef = null;
        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenInterfaceSignal)
            {
                IfgenInterfaceSignal signal = (IfgenInterfaceSignal) member;
                if (signal.getType() == IfgenSignalType.CLOCK)
                {
                    clockRef = new JavaVariableReference(
                        translateInterfaceSignal(cls, intf, signal, null, true,
                            null));
                    break;
                }
            }
        }
        if (clockRef == null)
        {
            System.out
                .println("Warning: Using DV.simulation.getDefaultClock() for interface "
                    + intf.getName());
            clockRef = ExpressionBuilder.memberCall(dvSimRef,
                defClockAccessorName);
        }
        assert (clockRef != null);

        // translate non-clock signals
        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenInterfaceSignal)
            {
                IfgenInterfaceSignal signal = (IfgenInterfaceSignal) member;
                if (signal.getType() != IfgenSignalType.CLOCK)
                {
                    translateInterfaceSignal(cls, intf, signal, null, true,
                        clockRef);
                }
            }
        }

        // create empty private ctor to prevent instantiation
        JavaConstructor ctor = cls.newConstructor();
        ctor.setVisibility(JavaVisibility.PRIVATE);
        ctor.setBody(new JavaBlock(javaSchema));

        return cls;
    }

    private JavaExpression makeMemberAccess(
        JavaExpression expr,
        JavaVariableReference intfRef)
    {
        if (expr instanceof JavaVariableReference)
        {
            return new JavaMemberAccess(intfRef,
                (JavaMemberVariable) ((JavaVariableReference) expr)
                    .getVariable());
        }
        else if (expr instanceof JavaArrayCreation)
        {
            JavaArrayCreation arrCreate = (JavaArrayCreation) expr;
            JavaArrayInitializer arrInit = new JavaArrayInitializer(arrCreate
                .getInitializer().getResultType());
            for (JavaExpression argExpr : arrCreate.getInitializer()
                .getElements())
            {
                makeMemberAccess(argExpr, intfRef);
                assert (argExpr instanceof JavaVariableReference);
                argExpr = new JavaMemberAccess(intfRef,
                    (JavaMemberVariable) ((JavaVariableReference) argExpr)
                        .getVariable());
                arrInit.addElement(argExpr);
            }
            arrCreate.setInitializer(arrInit);
            return arrCreate;
        }
        else if (expr instanceof JavaFunctionInvocation)
        {
            JavaFunctionInvocation funcInvoke = (JavaFunctionInvocation) expr;
            JavaFunctionInvocation newInvoke = new JavaFunctionInvocation(
                funcInvoke.getFunction());

            for (final JavaExpression funcExpr : funcInvoke.getArguments())
            {
                newInvoke.addArgument(makeMemberAccess(funcExpr, intfRef));
            }
            return newInvoke;
        }
        else if (expr instanceof Literal)
        {
            return expr;
        }
        else
        {
            throw new AssertionError("Unexpected type: " + expr.getClass());
        }
    }

    public JavaMemberVariable translateInterfaceSignal(
        IfgenInterfaceSignal signal)
    {
        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap.get(signal);
        if (var == null)
        {
            IfgenInterface intf = signal.getInterface();
            translateInterface(intf);
            var = (JavaMemberVariable) xlatObjMap.get(signal);
            assert (var != null);
        }
        return var;
    }

    private JavaMemberVariable translateInterfaceSignal(
        JavaRawClass cls,
        IfgenInterface intf,
        IfgenInterfaceSignal signal,
        JavaExpression intfNameExtension,
        boolean makeStatic,
        JavaExpression clockRef)
    {
        // get signal type and initializer
        final JavaInterface signalType;
        final JavaExpression initExpr;
        final IfgenSignalType type = signal.getType();

        final JavaExpression name;
        if (!intf.hasParameters())
        {
            name = new JavaStringLiteral(javaSchema,
                getSignalName(intf, signal));
        }
        else
        {
            name = new JavaAdd(javaSchema, new JavaAdd(javaSchema,
                new JavaStringLiteral(javaSchema, intf.getName()
                    .getIdentifier()
                    + "_"), intfNameExtension), new JavaStringLiteral(
                javaSchema, "_" + signal.getName().getIdentifier()));
        }

        if (type == IfgenSignalType.CLOCK)
        {
            signalType = clockSignalType;
            initExpr = ExpressionBuilder.memberCall(dvSimRef, "getClockSignal",
                name, new JavaIntLiteral(javaSchema,
                    signal.getSampleDepth() + 1));
        }
        else if (type == IfgenSignalType.INPUT)
        {
            signalType = inputSignalType;
            initExpr = ExpressionBuilder
                .memberCall(dvSimRef, "getInputSignal",
                    new JavaExpression[] {
                        name,
                        clockRef,
                        getEdgeExpr(signal.getSample().getEdge()),
                        new JavaIntLiteral(javaSchema, signal.getSample()
                            .getSkew()),
                        new JavaIntLiteral(javaSchema,
                            signal.getSampleDepth() + 1) }, null);
        }
        else if (type == IfgenSignalType.OUTPUT)
        {
            signalType = outputSignalType;
            initExpr = ExpressionBuilder
                .memberCall(dvSimRef, "getOutputSignal",
                    new JavaExpression[] {
                        name,
                        clockRef,
                        getEdgeExpr(signal.getDrive().getEdge()),
                        new JavaIntLiteral(javaSchema, signal.getDrive()
                            .getSkew()) }, null);
        }
        else
        {
            assert (type == IfgenSignalType.INOUT);

            signalType = inOutSignalType;
            initExpr = ExpressionBuilder
                .memberCall(dvSimRef, "getInOutSignal",
                    new JavaExpression[] {
                        name,
                        clockRef,
                        getEdgeExpr(signal.getSample().getEdge()),
                        new JavaIntLiteral(javaSchema, signal.getSample()
                            .getSkew()),
                        getEdgeExpr(signal.getDrive().getEdge()),
                        new JavaIntLiteral(javaSchema, signal.getDrive()
                            .getSkew()),
                        new JavaIntLiteral(javaSchema,
                            signal.getSampleDepth() + 1) }, null);
        }

        // create interface member variable
        String memberID = signal.getName().getIdentifier();
        JavaMemberVariable var = new JavaMemberVariable(memberID, signalType);
        var.setVisibility(JavaVisibility.PUBLIC);
        var.addModifier(JavaVariableModifier.FINAL);
        if (makeStatic)
        {
            var.addModifier(JavaVariableModifier.STATIC);
            var.setInitializer(initExpr);
        }
        cls.addMember(var);
        xlatObjMap.put(signal, var);
        signalInitMap.put(var, initExpr);
        return var;
    }

    static String getSignalName(IfgenInterface intf, IfgenInterfaceSignal signal)
    {
        return intf.getName().getIdentifier() + "_"
            + signal.getName().getIdentifier();
    }

    private JavaExpression getEdgeExpr(IfgenEdge e)
    {
        final String name;
        switch (e)
        {
        case POSEDGE:
            name = "POSEDGE";
            break;
        case NEGEDGE:
            name = "NEGEDGE";
            break;
        case ANYEDGE:
            name = "ANYEDGE";
            break;
        default:
            throw new AssertionError("Unknown edge: " + e);
        }
        return new JavaVariableReference(edgeSetType.getField(name));
    }

    public JavaRawClass translatePort(IfgenPort port)
    {
        JavaRawClass cls = (JavaRawClass) xlatObjMap.get(port);
        if (cls != null) return cls;

        // get package
        IfgenPackage pkg = port.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create port class
        String portID = port.getName().getIdentifier();
        cls = new JavaRawClass(javaSchema, portID, javaPkg);
        cls.setVisibility(JavaVisibility.PUBLIC);
        cls.addModifier(JavaTypeModifier.FINAL);
        if (javaPkg != null)
            javaPkg.addMember(cls);
        else
            javaSchema.addMember(cls);
        xlatObjMap.put(port, cls);
        xlatTypes.add(cls);

        cls.addAnnotation(new BlockComment("/ Generated port"));

        JavaFunctionType ctorFuncType = new JavaFunctionType(
            javaSchema.voidType);
        JavaBlock ctorBody = new JavaBlock(javaSchema);

        for (IfgenPortSignal member : port.getMembers())
        {
            String memberID = member.getName().getIdentifier();
            IfgenDirection dir = member.getDirection();
            JavaStructuredType memberType = signalTypeMap.get(dir);

            // create port member variable
            JavaMemberVariable var = cls.newField(memberID, memberType);
            var.setVisibility(JavaVisibility.PUBLIC);
            xlatObjMap.put(member, var);

            // add argument to bind constructor
            final JavaFunctionArgument arg = new JavaFunctionArgument(memberID,
                memberType);
            arg.addModifier(JavaVariableModifier.FINAL);
            ctorFuncType.addArgument(arg);

            // assign argument to member in bind constructor
            JavaMemberAccess varRef = new JavaMemberAccess(
                new JavaThisReference(cls), var);
            JavaVariableReference argRef = new JavaVariableReference(arg);
            JavaExpressionStatement assignStmt = new JavaExpressionStatement(
                new JavaAssign(javaSchema, varRef, argRef));
            ctorBody.addMember(assignStmt);
        }

        // create default constructor
        JavaConstructor defCtor = cls.newConstructor();
        defCtor.setVisibility(JavaVisibility.PUBLIC);
        defCtor.setBody(new JavaBlock(javaSchema));

        // create bind constructor
        JavaConstructor ctor = new JavaConstructor(ctorFuncType);
        ctor.setVisibility(JavaVisibility.PUBLIC);
        ctor.setBody(ctorBody);
        cls.addMember(ctor);

        return cls;
    }

    public JavaMemberVariable translatePortSignal(IfgenPortSignal signal)
    {
        JavaMemberVariable var = (JavaMemberVariable) xlatObjMap.get(signal);
        if (var == null)
        {
            translatePort(signal.getPort());
            var = (JavaMemberVariable) xlatObjMap.get(signal);
            assert (var != null);
        }
        return var;
    }

    public void translateHDLTask(IfgenHDLTask task)
    {    
	    if (task.hasParameters())
	    {
	    	translateParameterizedHDLTask(task);
	    }
	    else
	    {
	    	translateNonParameterizedHDLTask(task);
	    }
    }
    
    private JavaRawClass translateParameterizedHDLTask(IfgenHDLTask task)
    {
        JavaRawClass cls = (JavaRawClass) xlatObjMap.get(task);
        if (cls != null) return cls;

        // get package
        IfgenPackage pkg = task.getPackage();
        JavaPackage javaPkg = translatePackage(pkg);

        // create interface class
        String intfID = task.getName().getIdentifier();
        cls = new JavaRawClass(javaSchema, intfID, javaPkg);
        cls.addModifier(JavaTypeModifier.FINAL);
 
        cls.addAnnotation(new BlockComment(
                "/ Generated task wrappers for HDL Tasks in "
                    + pkg.getName().getCanonicalName()));
                
        if (javaPkg != null)
            javaPkg.addMember(cls);
        else
            javaSchema.addMember(cls);
        xlatObjMap.put(task, cls);
        xlatTypes.add(cls);
    	
        System.out.println("Translating hdl_task inst " + task.getName());

        cls.setVisibility(JavaVisibility.PUBLIC);
        IfgenSchema ifgenSchema = task.getSchema();
        for (final IfgenVariableDecl decl : task.getParameters())
        {
            // don't allow Bit parameters to hdl tasks
            JavaType type = convertParamArgType(ifgenSchema, decl.getType());
            JavaMemberVariable member = new JavaMemberVariable(decl
                    .getName().getIdentifier(), type);
            member.setVisibility(JavaVisibility.PUBLIC);
            member.addModifier(JavaVariableModifier.FINAL);
            cls.addMember(member);
        }
        
        // create private ctor to prevent instantiation that assigns all fields (i.e. this.x = x)
        {
            List<JavaClassMember> members = cls.getMembers();
            List<JavaFunctionArgument> ctorArgs = new LinkedList<JavaFunctionArgument>();
            for (final JavaClassMember member : members)
            {
                if (member instanceof JavaMemberVariable)
                {
                    JavaMemberVariable memberVar = (JavaMemberVariable) member;
                    JavaFunctionArgument ctorArg = new JavaFunctionArgument(
                        memberVar.getName().getIdentifier(), memberVar
                            .getType());
                    ctorArg.addModifier(JavaVariableModifier.FINAL);
                    ctorArgs.add(ctorArg);
                }
            }
            JavaConstructor ctor = new JavaConstructor(new JavaFunctionType(
                javaSchema.voidType, ctorArgs));
            JavaBlock body = new JavaBlock(javaSchema);
            ctor.setVisibility(JavaVisibility.PRIVATE);
            ctor.setBody(body);
            cls.addMember(ctor);

            // assign each member from its respective argument
            {
                for (final JavaFunctionArgument arg : ctorArgs)
                {
                	cls.getField(arg.getName()
                            .getIdentifier());
                    JavaExpression lhs = new JavaMemberAccess(
                        new JavaThisReference(cls), cls.getField(arg.getName()
                            .getIdentifier()));
                    JavaExpression rhs = new JavaVariableReference(arg);
                    body.addMember(new JavaExpressionStatement(new JavaAssign(
                        javaSchema, lhs, rhs)));
                }
            }
        }

        List<JavaFunctionArgument> templateArgs = new LinkedList<JavaFunctionArgument>();
        // invoke function
        {
	        JavaFunction func = null;
	        LinkedList<JavaExpression> outFuncArgs = new LinkedList<JavaExpression>();
	        
	        // create call expression
	        JavaExpression callExpression = new JavaStringLiteral(javaSchema, task.getName().getIdentifier());
            List<JavaClassMember> members = cls.getMembers();
            for (final JavaClassMember member : members)
            {
                if (member instanceof JavaMemberVariable)
                {
                    JavaMemberVariable memberVar = (JavaMemberVariable) member;
                    callExpression = new JavaAdd(javaSchema, callExpression, new JavaStringLiteral(javaSchema, "_"));
	                callExpression = new JavaAdd(javaSchema, callExpression, new JavaVariableReference(memberVar));
                }
            }	            
            outFuncArgs.add(callExpression);
            
	        {
	            // Create parameter args
	            for (final IfgenVariableDecl decl : task.getParameters())
	            {	            	
	                // don't allow Bit parameters to hdl tasks
	                JavaType type = convertParamArgType(ifgenSchema, decl.getType());
	                JavaFunctionArgument arg = new JavaFunctionArgument(decl
	                    .getName().getIdentifier(), type);
	                arg.addModifier(JavaVariableModifier.FINAL);
	                templateArgs.add(arg);
	            }
	            	
	            // Create task arguments
	            List<JavaFunctionArgument> inFuncArgs = new LinkedList<JavaFunctionArgument>();            
	            for (final IfgenTaskArg arg : task.getArguments())
	            {
	                // allow Bit arguments to hdl tasks if arg size == 1
	                JavaType type = convertParamArgType(ifgenSchema, arg.getType(),
	                    arg.getSize() == 1);
	                JavaAnnotation length = null;
	                if (arg.getType() == ifgenSchema.BIT_TYPE)
	                {
	                    if (arg.getDirection() == IfgenDirection.OUTPUT
	                        || arg.getDirection() == IfgenDirection.INOUT)
	                    {
	                        // needs to be an array ref. and MUST be a bitvector
	                        type = javaSchema.getArrayType(type, 1);
	                    }
	                    length = new JavaAnnotation(this.lengthType);
	                    length.setElementValue("value", new JavaIntLiteral(
	                        javaSchema, arg.getSize()));
	                }
	                JavaFunctionArgument fArg = new JavaFunctionArgument(arg
	                    .getName().getIdentifier(), type);
	                if (length != null)
	                {
	                    fArg.addAnnotation(length);
	                }
	                inFuncArgs.add(fArg);
	                outFuncArgs.add(new JavaVariableReference(fArg));
	            }
	
	            JavaFunctionType inFuncType = new JavaFunctionType(
	                javaSchema.voidType, inFuncArgs);
	            func = new JavaFunction("invoke", inFuncType);
	            func.setVisibility(JavaVisibility.PUBLIC);
	            cls.addMember(func);
	        }
	
	        // create the body of the accessor
	        {
	            try
	            {
	                JavaExpression[] expressionArray = new JavaExpression[outFuncArgs.size()];
	                outFuncArgs.toArray(expressionArray);
	                
	                JavaBlock body = new JavaBlock(javaSchema);
	                JavaVariableReference simRef = new JavaVariableReference(
	                    ((JavaRawClass) javaSchema.getTypeForClass(DV.class
	                        .getName())).getField("simulation"));
	                JavaFunctionInvocation invoke = ExpressionBuilder.memberCall(
	                    simRef, "callVerilogTask", expressionArray, cls);
	                body.addMember(new JavaExpressionStatement(invoke));
	                func.setBody(body);
	            }
	            catch (ClassNotFoundException e)
	            {
	                throw new Error(e);
	            }
	        }
        }
        
        // static get<CLASS>(arg0, arg1 ...)
        {            	
	        JavaFunctionType funcType = new JavaFunctionType(cls, templateArgs);
	        JavaFunction func = new JavaFunction("get"
	            + task.getName().getIdentifier(), funcType);
	        JavaBlock body = new JavaBlock(javaSchema);
	        func.setBody(body);
	        func.setVisibility(JavaVisibility.PUBLIC);
	        func.addModifier(JavaFunctionModifier.STATIC);	
	        cls.addMember(func);
	        
            
	        // return new <CLASS>(arg0, arg1 ...)
            JavaExpression[] templateArgsExpression = new JavaExpression[templateArgs.size()];
            int tempIdx = 0;
            for (final JavaFunctionArgument arg : templateArgs)
            {
            	templateArgsExpression[tempIdx++] = new JavaVariableReference(arg);
            }
            body.addMember(new JavaReturnStatement(javaSchema,
            		ExpressionBuilder.newInstance(cls, templateArgsExpression, cls)));
	        
        }
        return cls;
    }
    
    private void translateNonParameterizedHDLTask(IfgenHDLTask task)
    {
        final JavaRawClass taskClass = getTasksClass(task.getPackage());
        // check if this class already contains a wrapper for this task
        Set<JavaFunction> methods = taskClass.getMethods(task.getName()
            .getIdentifier(), null);
        if (!methods.isEmpty())
        {
            // no overloading is allowed. if this set isn't empty, the method
            // has already been added
            assert (methods.size() == 1);
            return;
        }

        IfgenSchema ifgenSchema = task.getSchema();

        // create the arguments to the method
        JavaFunction func = null;
        {
            // Create task arguments
            List<JavaFunctionArgument> funcArgs = new LinkedList<JavaFunctionArgument>();
            for (final IfgenTaskArg arg : task.getArguments())
            {
                // allow Bit arguments to hdl tasks if size is 1
                JavaType type = convertParamArgType(ifgenSchema, arg.getType(),
                    arg.getSize() == 1);
                JavaAnnotation length = null;
                if (arg.getType() == ifgenSchema.BIT_TYPE)
                {
                    if (arg.getDirection() == IfgenDirection.OUTPUT
                        || arg.getDirection() == IfgenDirection.INOUT)
                    {
                        // needs to be an array ref. and MUST be a bitvector
                        type = javaSchema.getArrayType(type, 1);
                    }
                    length = new JavaAnnotation(this.lengthType);
                    length.setElementValue("value", new JavaIntLiteral(
                        javaSchema, arg.getSize()));
                }
                JavaFunctionArgument fArg = new JavaFunctionArgument(arg
                    .getName().getIdentifier(), type);
                if (length != null)
                {
                    fArg.addAnnotation(length);
                }
                funcArgs.add(fArg);
            }

            JavaFunctionType funcType = new JavaFunctionType(
                javaSchema.voidType, funcArgs);
            func = new JavaFunction(task.getName().getIdentifier(), funcType);
            func.setVisibility(JavaVisibility.PUBLIC);
            func.addModifier(JavaFunctionModifier.STATIC);
            taskClass.addMember(func);
        }

        // create the body of the accessor
        {
            final int numArgs = task.getArguments().size();

            final JavaExpression[] funcArgs = new JavaExpression[1 + numArgs];
            funcArgs[0] = new JavaStringLiteral(javaSchema, task.getName()
                .getIdentifier());

            int idx = 1;
            for (final JavaFunctionArgument arg : func.getType().getArguments())
            {
                funcArgs[idx++] = new JavaVariableReference(arg);
            }

            try
            {
                JavaBlock body = new JavaBlock(javaSchema);
                JavaVariableReference simRef = new JavaVariableReference(
                    ((JavaRawClass) javaSchema.getTypeForClass(DV.class
                        .getName())).getField("simulation"));
                JavaFunctionInvocation invoke = ExpressionBuilder.memberCall(
                    simRef, "callVerilogTask", funcArgs, taskClass);
                body.addMember(new JavaExpressionStatement(invoke));
                func.setBody(body);
            }
            catch (ClassNotFoundException e)
            {
                throw new Error(e);
            }
        }
    }

    public void translateHVLTask(IfgenHVLTask obj)
    {
        // Nothing to do here
    }
}
