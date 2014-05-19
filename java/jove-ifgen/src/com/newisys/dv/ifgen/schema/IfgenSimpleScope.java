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

import java.util.Iterator;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Scope;

/**
 * Scope implementation that delegates to an associated scope, then the
 * enclosing scope.
 * 
 * @author Jon Nall
 */
public final class IfgenSimpleScope
    extends IfgenScopeDelegate
{
    private Scope scope;

    public IfgenSimpleScope(Scope scope, IfgenScopeDelegate enclosingScope)
    {
        super(enclosingScope);
        this.scope = scope;
    }

    public Iterator< ? extends NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        Iterator< ? extends NamedObject> iter = scope.lookupObjects(identifier,
            kind);
        if (!iter.hasNext() && enclosingScope != null)
        {
            iter = enclosingScope.lookupObjects(identifier, kind);
        }
        return iter;
    }
}
