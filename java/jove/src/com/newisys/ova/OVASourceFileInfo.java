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
 * Class representing the OVASrcFileBlk structure of OVA.
 * 
 * @author Jon Nall
 */
public final class OVASourceFileInfo
{
    public final String fileName;
    public final int startRow;
    public final int startColumn;
    public final int endRow;
    public final int endColumn;

    public OVASourceFileInfo(
        String fileName,
        int startRow,
        int startColumn,
        int endRow,
        int endColumn)
    {
        this.fileName = fileName;
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;
    }
}
