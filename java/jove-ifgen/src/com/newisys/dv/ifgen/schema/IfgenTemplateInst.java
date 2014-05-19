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

import java.util.List;

/**
 * Ifgen schema object for template instances.
 * 
 * @author Jon Nall
 */
public final class IfgenTemplateInst
    extends IfgenNamedTestbenchMember
{
    private final IfgenName instanceName;
    private final IfgenUnresolvedName templateName;
    private final IfgenTemplateKind kind;
    private final List<IfgenExpression> arguments;
    private IfgenTemplate template;

    public IfgenTemplateInst(
        IfgenSchema schema,
        IfgenUnresolvedName templateName,
        IfgenName instanceName,
        IfgenTemplateKind kind,
        List<IfgenExpression> arguments)
    {
        super(schema, instanceName);
        this.instanceName = instanceName;
        this.templateName = templateName;
        this.kind = kind;
        this.arguments = arguments;
    }

    public List<IfgenExpression> getArgs()
    {
        return arguments;
    }

    public IfgenTemplateKind getTemplateKind()
    {
        return kind;
    }

    public IfgenName getInstanceName()
    {
        return instanceName;
    }

    public IfgenUnresolvedName getUnresolvedTemplateName()
    {
        return templateName;
    }

    public void setTemplate(IfgenTemplate template)
    {
        this.template = template;
    }

    public IfgenTemplate getTemplate()
    {
        return template;
    }
}
