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

package com.newisys.ova;

/**
 * Class representing the OVASyntaxInfo structure.
 * 
 * @author Jon Nall
 */
public final class OVAAssertInfo
{
    public final String name;
    public final OVAExprType exprType;
    public final OVASourceFileInfo srcFileBlk;
    public final int severity; // 8 bits
    public final int category; // 24 bits
    public final String scopeName;
    public final String userMsg;

    public OVAAssertInfo(
        String name,
        OVAExprType exprType,
        OVASourceFileInfo srcFileBlk,
        int severity,
        int category,
        String scopeName,
        String userMsg)
    {
        assert ((severity & ~0xFF) == 0);
        assert ((category & ~0xFFFFFF) == 0);
        assert (userMsg != null);

        this.name = name;
        this.exprType = exprType;
        this.srcFileBlk = srcFileBlk;
        this.severity = severity;
        this.category = category;
        this.scopeName = scopeName;
        this.userMsg = userMsg;
    }
}
