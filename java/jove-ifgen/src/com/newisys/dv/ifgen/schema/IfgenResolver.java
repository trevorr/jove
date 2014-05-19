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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Namespace;
import com.newisys.langschema.Scope;

/**
 * Resolves names to schema objects.
 * 
 * @author Trevor Robinson
 */
public final class IfgenResolver
{
    private final IfgenSchema schema;
    private final IfgenPackage pkg;
    private List<Namespace> typeImports = new LinkedList<Namespace>();
    private List<Namespace> demandImports = new LinkedList<Namespace>();

    public IfgenResolver(IfgenSchema schema, IfgenPackage pkg)
    {
        this.schema = schema;
        this.pkg = pkg;
    }

    public void addTypeImport(Namespace type)
    {
        typeImports.add(type);
    }

    public void addDemandImport(Namespace packageOrType)
    {
        demandImports.add(packageOrType);
    }

    public NamedObject resolveName(IfgenUnresolvedName name, IfgenNameKind kind)
        throws IfgenResolverException
    {
        return resolveName(name, kind, false);
    }

    public NamedObject resolveName(
        IfgenUnresolvedName name,
        IfgenNameKind kind,
        boolean canonicalOnly)
        throws IfgenResolverException
    {
        return resolveName(name, kind, canonicalOnly, name.getIdentifiers()
            .size() - 1);
    }

    private NamedObject resolveName(
        IfgenUnresolvedName name,
        IfgenNameKind kind,
        boolean canonicalOnly,
        int pos)
        throws IfgenResolverException
    {
        NamedObject obj = null;
        String id = name.getIdentifiers().get(pos);

        Scope scope;
        if (pos > 0)
        {
            // recursively get scope from qualifier
            IfgenNameKind prevKind;
            switch (kind)
            {
            case TYPE:
                prevKind = IfgenNameKind.PACKAGE_OR_TYPE;
                break;
            case EXPRESSION:
            case METHOD:
                prevKind = IfgenNameKind.AMBIGUOUS;
                break;
            default:
                prevKind = kind;
            }
            scope = checkNamespace(resolveName(name, prevKind, canonicalOnly,
                pos - 1));
        }
        else
        {
            // check for imported type
            if (!canonicalOnly && kind == IfgenNameKind.TYPE)
            {
                for (Namespace type : typeImports)
                {
                    if (type.getName().getIdentifier().equals(id))
                    {
                        obj = type;
                        break;
                    }
                }
            }

            // look for ID in package, if any
            if (obj == null && pkg != null && !canonicalOnly)
            {
                obj = lookupSingle(id, kind, pkg);
            }

            // look in schema for package or type in unnamed package
            scope = schema;
        }

        // look for ID in scope of qualifier or package
        if (obj == null)
        {
            obj = lookupSingle(id, kind, scope);
        }

        // look for ID in demand imported types
        if (obj == null && !canonicalOnly)
        {
            for (Namespace importScope : demandImports)
            {
                obj = lookupSingle(id, kind, importScope);
                if (obj != null) break;
            }
        }

        // unable to resolve name
        if (obj == null)
        {
            throw new IfgenResolverException("Unresolved name: "
                + name.toString(pos));
        }

        return obj;
    }

    private void resolveVariableDecls(
        IfgenSchemaObject container,
        List<IfgenVariableDecl> decls)
        throws IfgenResolverException
    {
        // set types are only allowed in testbenches
        final boolean setsAllowed = (container instanceof IfgenTestbench);

        final Set<String> identifiers = new HashSet<String>();
        // Check that sets are not used where they're not allowed
        // Check that any unresolved enum types are resolved
        for (final IfgenVariableDecl p : decls)
        {
            final IfgenType type = p.getType();
            if (type instanceof IfgenSetType && !setsAllowed)
            {
                throw new IfgenResolverException(
                    "Sets are not allowed for this type: "
                        + container.getClass());
            }

            else if (type instanceof IfgenUnresolvedEnumType)
            {
                // need to resolve this enum type
                IfgenUnresolvedEnumType unresolvedType = (IfgenUnresolvedEnumType) type;
                NamedObject obj = resolveName(unresolvedType
                    .getUnresolvedName(), IfgenNameKind.TYPE);
                if (!(obj instanceof IfgenEnum))
                {
                    throw new IfgenResolverException(unresolvedType
                        .getUnresolvedName()
                        + " is not an enumeration");
                }
                p.getVariable().setType(((IfgenEnum) obj).getType());
            }

            // check for unique identifiers
            String id = p.getName().getIdentifier();
            if (identifiers.contains(id))
            {
                throw new IfgenResolverException(
                    "Duplicate identifier in variable declarations: " + id);
            }
            identifiers.add(id);
        }
    }

    private void resolveParameterArguments(
        Scope argScope,
        List<IfgenExpression> args,
        IfgenTemplate template)
        throws IfgenResolverException
    {
        List<IfgenVariableDecl> params = template.getParameters();
        if (args.size() != params.size())
        {
            throw new IfgenResolverException(
                "Incorrect number of parameters to template: "
                    + template.getName());
        }

        // Check that each parameter is of the correct type
        for (int i = 0; i < args.size(); ++i)
        {
            final IfgenExpression expr = args.get(i);
            final IfgenVariableDecl parameter = params.get(i);

            // a type of null means this variable's type needs to be resolved
            // this occurs for unqualified enum literals and variable references
            // we also must do resolution for IfgenComplexVariableRefs. These
            // know their result type (string), but the variables probably need
            // to be resolved
            if (expr.getType() == null
                || expr instanceof IfgenComplexVariableRef)
            {
                if (expr instanceof IfgenEnumLiteral)
                {
                    IfgenEnumLiteral enumLit = (IfgenEnumLiteral) expr;
                    if (enumLit.getType() == null)
                    {
                        enumLit.setElement(resolveEnumLiteral(parameter,
                            enumLit.getUnresolvedName()));
                    }
                }
                else if (expr instanceof IfgenComplexVariableRef)
                {
                    IfgenComplexVariableRef r = (IfgenComplexVariableRef) expr;
                    for (final IfgenVariableRef ref : r.getVariableRefs())
                    {
                        if (ref.getDecl() == null)
                        {
                            resolveVariable(argScope, ref);
                        }
                    }
                }
                else if (expr instanceof IfgenVariableRef)
                {
                    // Resolve the variable reference if need be
                    IfgenVariableRef r = (IfgenVariableRef) expr;
                    if (r.getDecl() == null)
                    {
                        resolveVariable(argScope, r);
                    }
                    assert (!(r.getType() instanceof IfgenUnresolvedEnumType));
                }
                else
                {
                    System.err
                        .println("Oops! Didn't know I had to resolve type: "
                            + expr.getType());
                    assert (false);
                }
            }

            // all param/arg types are now resolved. make sure they're the same
            if (expr.getType() != parameter.getType())
            {
                throw new IfgenResolverException("Wrong type for parameter "
                    + i + " to " + template.getName() + ". Expected: "
                    + parameter.getType() + ", Actual: " + expr.getType());
            }
        }
    }

    private IfgenEnumElement resolveEnumLiteral(
        IfgenVariableDecl expectDecl,
        IfgenUnresolvedName name)
        throws IfgenResolverException
    {
        // Check that the expected value is, in fact, an enumeration type
        IfgenType type = expectDecl.getVariable().getType();
        if (!(type instanceof IfgenEnumType))
        {
            throw new IfgenResolverException(
                "Attempting to pass enumeration as a string parameter: " + name);
        }

        IfgenEnum e = ((IfgenEnumType) type).getEnumeration();
        List<String> ids = name.getIdentifiers();
        final String enumValue = ids.get(ids.size() - 1);

        // Allow qualified names.
        if (ids.size() > 1)
        {
            NamedObject obj = resolveName(name, IfgenNameKind.EXPRESSION);
            if (!(obj instanceof IfgenEnumElement))
            {
                throw new IfgenResolverException(obj
                    + " is not an enumeration element");
            }

            return (IfgenEnumElement) obj;
        }

        // Not a qualified name. Must search through the expected type explicitly
        for (final IfgenEnumElement element : e.getElements())
        {
            if (enumValue.equals(element.getName().getIdentifier()))
            {
                return element;
            }
        }

        throw new IfgenResolverException("Cannot find enumeration element "
            + enumValue + " in enumeration type: " + expectDecl.getType());
    }

    private IfgenVariableDecl resolveVariable(
        Scope scope,
        IfgenVariableRef varRef)
        throws IfgenResolverException
    {
        IfgenVariableDecl decl = null;
        List<String> ids = varRef.getUnresolvedName().getIdentifiers();

        // In 2 cases we implicitly convert to a string
        // 1. this is a qualified name (ids.size() > 1)
        // 2. there is a variable at a position other than 0
        String name = ids.get(0);
        final int lastDollar = name.lastIndexOf('$');
        if (ids.size() > 1 || lastDollar > 0)
        {
            IfgenName internalName = new IfgenName(nextInternalName(),
                IfgenNameKind.EXPRESSION);
            IfgenVariable var = new IfgenVariable(internalName,
                schema.STRING_TYPE);
            decl = new IfgenVariableDecl(schema, var);
        }
        else if (lastDollar < 0)
        {
            throw new IfgenResolverException("Name is not a variable: "
                + varRef);
        }
        else
        {
            // Remove the leading '$'
            name = name.substring(1);
            if (name.charAt(0) == '{')
            {
                // remove enclosing "{" and "}"
                name = name.substring(1, name.length() - 1);
            }
            NamedObject obj = lookupSingle(name, IfgenNameKind.EXPRESSION,
                scope);
            if (!(obj instanceof IfgenVariableDecl))
            {
                throw new IfgenResolverException(name + " is not a variable");
            }

            if (obj == null)
            {
                throw new IfgenResolverException("Unresolved variable: "
                    + varRef);
            }
            else
            {
                decl = (IfgenVariableDecl) obj;
            }
        }

        varRef.setDecl(decl);
        return decl;
    }

    static NamedObject lookupSingle(String id, IfgenNameKind kind, Scope scope)
    {
        Iterator< ? extends NamedObject> iter = scope.lookupObjects(id, kind);
        return iter.hasNext() ? iter.next() : null;
    }

    private static Namespace checkNamespace(NamedObject obj)
        throws IfgenResolverException
    {
        if (!(obj instanceof Namespace))
        {
            throw new IfgenResolverException("Package/type name expected: "
                + obj.getName());
        }
        return (Namespace) obj;
    }

    private static int internalVarNum = 0;

    private static String nextInternalName()
    {
        return "__INTERNAL" + internalVarNum++;
    }

    public static void resolve(IfgenSchema schema, Collection<IfgenFile> files)
        throws IfgenResolverException
    {
        for (IfgenFile file : files)
        {
            IfgenResolver resolver = new IfgenResolver(schema, file
                .getPackageDecl());
            resolver.resolve(file);
        }
    }

    public void resolve(IfgenFile file)
        throws IfgenResolverException
    {
        processImports(file);

        for (IfgenSchemaMember member : file.getDefinitions())
        {
            if (member instanceof IfgenHDLTask)
            {
                IfgenHDLTask task = (IfgenHDLTask) member;
                if (task.getInstancePath().containsVarRef())
                {
                    // path contains a variable -- resolve it
                    IfgenComplexVariableRef complexRef = task.getInstancePath().getComplexRef();
                    for (final IfgenVariableRef ref : complexRef
                        .getVariableRefs())
                    {
                        resolveVariable(task, ref);
                    }
                }
            }
            if (member instanceof IfgenInterface)
            {
                IfgenInterface intf = (IfgenInterface) member;
                resolveInterfaceMembers(intf);
            }
            else if (member instanceof IfgenBind)
            {
                IfgenBind bind = (IfgenBind) member;

                IfgenUnresolvedName portName = bind.getPortName();
                NamedObject obj = resolveName(portName, IfgenNameKind.TYPE);
                if (!(obj instanceof IfgenPort))
                {
                    throw new IfgenResolverException(portName
                        + " is not a port");
                }
                IfgenPort port = (IfgenPort) obj;
                bind.setPort(port);

                resolveVariableDecls((IfgenSchemaObject) bind, bind
                        .getParameters());
                
                resolveBindMembers(bind);
            }
            else if (member instanceof IfgenTestbench)
            {
                IfgenTestbench tb = (IfgenTestbench) member;

                // Handle imports
                for (IfgenWildname names : tb.getImports())
                {
                    final IfgenUnresolvedName qname = names
                        .getPackageOrTypeName();
                    final NamedObject obj = resolveName(qname,
                        IfgenNameKind.AMBIGUOUS);
                    assert (names.isImportMembers());
                    if (!(obj instanceof IfgenPackage))
                    {
                        throw new IfgenResolverException(
                            "Wildcard testbench import '" + qname
                                + "' is not a package");
                    }

                    IfgenPackage pkg = (IfgenPackage) obj;
                    boolean found = false;
                    for (IfgenPackageMember pkgMember : pkg.getMembers())
                    {
                        if (pkgMember instanceof IfgenInterface)
                        {
                            tb.addMember((IfgenInterface) pkgMember);
                            found = true;
                        }
                        else if (pkgMember instanceof IfgenTask)
                        {
                            //tb.addToBeginMember((IfgenTask) pkgMember);
                        	tb.addMember((IfgenTask) pkgMember);
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        throw new IfgenResolverException(
                            "Wildcard shell member '" + qname
                                + "' references no interfaces or tasks");
                    }

                    addDemandImport((IfgenPackage) obj);
                }

                resolveTestbenchMembers(new IfgenSimpleScope(tb, null), tb,
                    new HashSet<String>());
            }
        }
    }

    private void processImports(IfgenFile file)
        throws IfgenResolverException
    {
        for (IfgenWildname importDecl : file.getImportDecls())
        {
            IfgenUnresolvedName name = importDecl.getPackageOrTypeName();
            if (!importDecl.isImportMembers())
            {
                Namespace scope = checkNamespace(resolveName(name,
                    IfgenNameKind.TYPE, true));
                addTypeImport(scope);
            }
            else
            {
                Namespace scope = checkNamespace(resolveName(name,
                    IfgenNameKind.PACKAGE_OR_TYPE, true));
                addDemandImport(scope);
            }
        }
    }

    private void resolveInterfaceMembers(IfgenInterface intf)
        throws IfgenResolverException
    {
        IfgenSampleDef defaultSample = null;
        IfgenDriveDef defaultDrive = null;
        IfgenModuleDef defaultModule = null;
        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenSampleDef)
            {
                defaultSample = (IfgenSampleDef) member;
            }
            else if (member instanceof IfgenDriveDef)
            {
                defaultDrive = (IfgenDriveDef) member;
            }
            else if (member instanceof IfgenModuleDef)
            {
                defaultModule = (IfgenModuleDef) member;
                if (defaultModule.containsVarRef())
                {
                    // module contains a variable -- resolve it
                    IfgenComplexVariableRef complexRef = defaultModule
                        .getComplexRef();
                    for (final IfgenVariableRef ref : complexRef
                        .getVariableRefs())
                    {
                        resolveVariable(intf, ref);
                    }
                }
            }
            else
            {
                IfgenInterfaceSignal signal = (IfgenInterfaceSignal) member;
                IfgenSignalType type = signal.getType();
                if (type.isInput() && type != IfgenSignalType.CLOCK)
                {
                    if (signal.getSample() == null)
                    {
                        if (defaultSample != null)
                        {
                            signal.setSample(defaultSample);
                        }
                        else
                        {
                            throw new IfgenResolverException(
                                "No explicit or default sample specification for signal "
                                    + signal.getName());
                        }
                    }
                }
                if (type.isOutput())
                {
                    if (signal.getDrive() == null)
                    {
                        if (defaultDrive != null)
                        {
                            signal.setDrive(defaultDrive);
                        }
                        else
                        {
                            throw new IfgenResolverException(
                                "No explicit or default drive specification for signal "
                                    + signal.getName());
                        }
                    }
                }

                IfgenSignalRef effHDLNode = signal.getHDLNode();
                if (effHDLNode == null)
                {
                    IfgenModuleDef module = signal.getModule();
                    if (module == null) module = defaultModule;
                    if (module != null)
                    {
                        IfgenUnresolvedName modName = module.getName();
                        if (module.containsVarRef())
                        {
                            IfgenComplexVariableRef complexRef = module
                                .getComplexRef();
                            for (final IfgenVariableRef ref : complexRef
                                .getVariableRefs())
                            {
                                resolveVariable(intf, ref);
                            }
                            signal.setModule(module);
                        }
                        else
                        {
                            String signalID = signal.getName().getIdentifier();

                            effHDLNode = new IfgenHDLSignalRef(
                                intf.getSchema(), modName + "." + signalID);
                        }
                    }
                    else
                    {
                        throw new IfgenResolverException(
                            "Module not found for signal "
                                + signal.getName().getIdentifier() + " in "
                                + "interface " + intf.getName());
                    }
                }
                if (effHDLNode != null)
                {
                    if (effHDLNode instanceof IfgenHDLSignalRef)
                    {
                        IfgenExpression expr = ((IfgenHDLSignalRef) effHDLNode)
                            .getHDL();
                        if (expr instanceof IfgenComplexVariableRef)
                        {
                            IfgenComplexVariableRef complexRef = (IfgenComplexVariableRef) expr;
                            for (final IfgenVariableRef ref : complexRef
                                .getVariableRefs())
                            {
                                resolveVariable(intf, ref);
                            }
                        }
                    }
                    signal.setEffectiveHDLNode(effHDLNode);
                }
            }
        }
    }

    private void resolveBindMembers(IfgenBind bind)
        throws IfgenResolverException
    {
        IfgenPort port = bind.getPort();
        SignalRefResolver signalRefResolver = new SignalRefResolver();
        for (IfgenBindMember member : bind.getMembers())
        {
            if (member instanceof IfgenInterfaceDef)
            {
                IfgenInterfaceDef intfDef = (IfgenInterfaceDef) member;
                IfgenUnresolvedName intfName = intfDef.getIntfName();
                NamedObject obj = resolveName(intfName, IfgenNameKind.TYPE);
                if (!(obj instanceof IfgenInterface))
                {
                    throw new IfgenResolverException(intfName
                        + " is not an interface");
                }

                IfgenInterface intf = (IfgenInterface) obj;

                // Verify bind parameters and resolve any unresolved types (i.e. enums)
                resolveVariableDecls(intf, intf.getParameters());
                // Verify args to check types and resolve any unresolved types
                resolveParameterArguments(bind, intfDef.getArgs(), intf);

                intfDef.setIntf(intf);
                signalRefResolver.setDefaultIntf(intf);
            }
            else
            {
                IfgenBindSignal bindSignal = (IfgenBindSignal) member;

                String portSignalID = bindSignal.getPortSignalID();
                NamedObject obj = lookupSingle(portSignalID,
                    IfgenNameKind.EXPRESSION, port);
                if (!(obj instanceof IfgenPortSignal))
                {
                    throw new IfgenResolverException("Signal " + portSignalID
                        + " not found in port " + port.getName());
                }
                IfgenPortSignal portSignal = (IfgenPortSignal) obj;
                bindSignal.setPortSignal(portSignal);

                IfgenSignalRef ref = bindSignal.getIntfSignal();
                signalRefResolver.resolve(ref);
            }
        }
    }

    private class SignalRefResolver
        implements IfgenSignalRefVisitor
    {
        private IfgenInterface defaultIntf;

        public void setDefaultIntf(IfgenInterface defaultIntf)
        {
            this.defaultIntf = defaultIntf;
        }

        public void resolve(IfgenSignalRef ref)
            throws IfgenResolverException
        {
            try
            {
                ref.accept(this);
            }
            catch (ResolverExceptionWrapper e)
            {
                throw e.cause;
            }
        }

        public void visit(IfgenConcatSignalRef obj)
        {
            for (IfgenSignalRef member : obj.getMembers())
            {
                member.accept(this);
            }
        }

        public void visit(IfgenHDLSignalRef obj)
        {
            // nothing to do
        }

        public void visit(IfgenInterfaceSignalRef obj)
        {
            try
            {
                NamedObject lookupObj;
                IfgenUnresolvedName name = obj.getSignalName();
                if (name.getIdentifiers().size() == 1)
                {
                    String id = name.getIdentifiers().get(0);
                    if (defaultIntf != null)
                    {
                        lookupObj = lookupSingle(id, IfgenNameKind.EXPRESSION,
                            defaultIntf);
                        if (!(lookupObj instanceof IfgenInterfaceSignal))
                        {
                            throw new IfgenResolverException("Signal " + id
                                + " not found in default interface "
                                + defaultIntf.getName());
                        }
                    }
                    else
                    {
                        throw new IfgenResolverException(
                            "Default interface required for simple signal reference: "
                                + id);
                    }
                }
                else
                {
                    lookupObj = resolveName(name, IfgenNameKind.EXPRESSION);
                    if (!(lookupObj instanceof IfgenInterfaceSignal))
                    {
                        throw new IfgenResolverException(name
                            + " is not an interface signal");
                    }
                }
                IfgenInterfaceSignal intfSignal = (IfgenInterfaceSignal) lookupObj;
                obj.setSignal(intfSignal);

            }
            catch (IfgenResolverException e)
            {
                throw new ResolverExceptionWrapper(e);
            }
        }

        public void visit(IfgenSliceSignalRef obj)
        {
            obj.getSignal().accept(this);
        }
    }

    private IfgenType resolveSetType(
        Scope scope,
        IfgenVariableDecl expectDecl,
        IfgenExpression expr)
        throws IfgenResolverException
    {
        IfgenType type = null;
        IfgenType expectedType = expectDecl.getType();

        IfgenSchema schema = expr.getSchema();
        if (expr instanceof IfgenIntegerLiteral)
        {
            type = schema.INTEGER_TYPE;
        }
        else if (expr instanceof IfgenStringLiteral)
        {
            type = schema.STRING_TYPE;
        }
        else if (expr instanceof IfgenEnumLiteral)
        {
            IfgenEnumLiteral literal = (IfgenEnumLiteral) expr;
            literal.setElement(resolveEnumLiteral(expectDecl, literal
                .getUnresolvedName()));
            assert (literal.getType() != null);
            type = literal.getType();
        }
        else if (expr instanceof IfgenVariableRef)
        {
            IfgenVariableRef ref = (IfgenVariableRef) expr;
            resolveVariable(scope, ref);
            assert (ref.getType() != null);
            type = ref.getType();
        }
        else if (expr instanceof IfgenSetLiteral)
        {
            IfgenSetLiteral lit = (IfgenSetLiteral) expr;
            IfgenSet s = lit.getSet();
            for (IfgenExpression e : s.getExpressions())
            {
                IfgenType memberType = resolveSetType(scope, expectDecl, e);
                assert (memberType != null);

                if (type == null)
                {
                    type = schema.addSetType(memberType);
                    lit.setMemberType(memberType);
                }
            }
            for (IfgenRange r : s.getRanges())
            {
                IfgenType fromType = resolveSetType(scope, expectDecl, r
                    .getFromExpr());
                IfgenType toType = resolveSetType(scope, expectDecl, r
                    .getToExpr());
                if (fromType != toType)
                {
                    throw new Error("Inconsistent types in range: " + expr);
                }
                if (type == null)
                {
                    type = schema.addSetType(fromType);
                    lit.setMemberType(fromType);
                }
            }
        }
        else if (expr instanceof IfgenSetOperator)
        {
            IfgenSetOperator setOp = (IfgenSetOperator) expr;
            IfgenType lhsType = resolveSetType(scope, expectDecl, setOp
                .getLHS());
            resolveSetType(scope, expectDecl, setOp.getRHS());
            type = schema.addSetType(lhsType);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported set type: "
                + expr.getClass());
        }

        if (type != expectedType)
        {
            boolean doError = true;
            if (type instanceof IfgenSetType)
            {
                if (((IfgenSetType) type).getMemberType() == expectedType)
                {
                    doError = false;
                }
            }

            if (doError)
            {
                throw new Error("Unexpected type. Observed: " + type
                    + ", Expected: " + expectedType);
            }
        }
        return type;
    }

    private void resolveTestbenchMembers(
        IfgenScopeDelegate scope,
        IfgenTestbenchMemberContainer container,
        Set<String> identifiers)
        throws IfgenResolverException
    {
        // Handle non-imports
        for (final IfgenTestbenchMember member : container.getMembers())
        {
            if (member instanceof IfgenForStatement)
            {
                IfgenForStatement forStmt = (IfgenForStatement) member;
                List<IfgenVariableDecl> decl = new LinkedList<IfgenVariableDecl>();
                decl.add(forStmt.getVarDecl());
                resolveVariableDecls(forStmt, decl);

                // resolve the type of the set over which we'll be iterating
                resolveSetType(scope, forStmt.getVarDecl(), forStmt.getSet());

                resolveTestbenchMembers(new IfgenSimpleScope(forStmt, scope),
                    (IfgenForStatement) member, identifiers);
            }
            else if (member instanceof IfgenTemplateInst)
            {
                IfgenTemplateInst templateInst = (IfgenTemplateInst) member;
                IfgenUnresolvedName templName = templateInst
                    .getUnresolvedTemplateName();
                NamedObject obj = null;
                switch (templateInst.getTemplateKind())
                {
                case INTERFACE:
                    obj = resolveName(templName, IfgenNameKind.TYPE);
                    if (!(obj instanceof IfgenInterface))
                    {
                        throw new IfgenResolverException(templName
                            + " is not an interface");
                    }
                    break;
                case BIND:
                    obj = resolveName(templName, IfgenNameKind.TYPE);
                    if (!(obj instanceof IfgenBind))
                    {
                        throw new IfgenResolverException(templName
                            + " is not a bind");
                    }
                    break;
                case HDL_TASK:
                    obj = resolveName(templName, IfgenNameKind.METHOD);
                    if (!(obj instanceof IfgenHDLTask))
                    {
                        throw new IfgenResolverException(templName
                            + " is not an hdl_task");
                    }
                    break;
                default:
                    throw new IfgenResolverException("Unknown template type: "
                        + templateInst.getTemplateKind());
                }

                // check for unique names
                String id = templateInst.getName().getIdentifier();
                if (identifiers.contains(id))
                {
                    throw new IfgenResolverException(
                        "Duplicate identifier found in template instantiation: "
                            + id);
                }
                identifiers.add(id);

                // resolve the parameters of the template definition and the
                // arguments of the template instantiation
                IfgenTemplate templDef = (IfgenTemplate) obj;
                templateInst.setTemplate(templDef);
                // Verify template parameters and resolve any unresolved types (i.e. enums)
                resolveVariableDecls((IfgenSchemaObject) templDef, templDef
                    .getParameters());
                // Verify args to check types and resolve any unresolved types
                resolveParameterArguments(scope, templateInst.getArgs(),
                    templDef);
            }
        }
    }

    private static class ResolverExceptionWrapper
        extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        final IfgenResolverException cause;

        public ResolverExceptionWrapper(IfgenResolverException cause)
        {
            super(cause);
            this.cause = cause;
        }
    }
}
