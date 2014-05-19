/*
 * Jove Constraint-based Random Solver
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

package com.newisys.randsolver.finalrandfinder;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randc;
import com.newisys.randsolver.annotation.Randomizable;

public final class FinalRandFinder
{
    private static class ClassDirFilter
        implements FileFilter
    {
        // This isn't perfect, but it will do
        private static String RANDSOLVER_TEST_DIR = File.separator
            + "randsolver" + File.separator + "bin" + File.separator + "com"
            + File.separator + "newisys" + File.separator + "randsolver";

        public boolean accept(File pathname)
        {
            // ignore hidden directories/files
            if (pathname.getAbsolutePath().contains(File.separator + "."))
            {
                return false;
            }
            // ignore classfiles from the randsolver test directory
            else if (pathname.getAbsolutePath().contains(RANDSOLVER_TEST_DIR))
            {
                return false;
            }
            else if (pathname.isDirectory())
            {
                return true;
            }
            else if (pathname.getPath().endsWith(".class"))
            {
                return true;
            }
            return false;
        }
    }

    /**
     * Returns all files under <code>directory</code> matching
     * <code>filter</code>.
     *
     * @param directory the directory under which to search
     * @param filter the filter to use when searching
     * @return a list of the files found
     */
    private List<File> getFilesRecursively(File directory, FileFilter filter)
    {
        //        System.out.println("Checking: " + directory.getAbsolutePath());
        assert (directory.isDirectory());
        List<File> fileList = new LinkedList<File>();

        File[] curDirContents = directory.listFiles(filter);
        for (final File f : curDirContents)
        {
            if (f.isDirectory())
            {
                fileList.addAll(getFilesRecursively(f, filter));
            }
            else
            {
                fileList.add(f);
            }
        }

        return fileList;
    }

    private ClassDirFilter classDirFilter = new ClassDirFilter();

    /**
     * Loads the class associated with <code>classFile</code> and checks to see
     * if any fields are @Rand/@Randc and final.
     *
     * @param classFile the class to check
     * @return true if final rand fields are found, false otherwise
     */
    private boolean checkClass(File classFile)
    {
        boolean foundRandFinal = false;

        ClassLoader cLoader = ClassLoader.getSystemClassLoader();
        Class< ? > klass = null;
        try
        {
            assert (!classFile.isDirectory());
            String pathName = classFile.getAbsolutePath();
            assert (pathName.contains(File.separator + "bin" + File.separator));
            String className = pathName.replaceFirst(
                ".*?" + File.separator + "bin" + File.separator, "")
                .replaceFirst("\\.class$", "").replace('/', '.');
            klass = cLoader.loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            System.out.println(e);
            System.exit(1);
        }

        Field[] fields = klass.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (final Field f : fields)
        {
            boolean isRand = f.isAnnotationPresent(Rand.class);
            boolean isRandc = f.isAnnotationPresent(Randc.class);
            boolean isRandomizable = f.getType().isAnnotationPresent(
                Randomizable.class);

            if (!isRandomizable && (isRand || isRandc))
            {
                if (Modifier.isFinal(f.getModifiers()))
                {
                    System.out.println("Field is @Rand/@Randc and final: "
                        + f.getDeclaringClass() + ": " + f.getType() + " "
                        + Modifier.toString(f.getModifiers()) + " "
                        + f.getName());
                    foundRandFinal |= true;
                }
            }
        }

        return foundRandFinal;
    }

    /**
     * Checks all class files in a directory to see if final rand fields are
     * present in any of the classfiles under that directory.
     *
     * @param dirName the name of the directory to check
     * @return true if final rand fields are found, false otherwise
     */
    private boolean checkDir(String dirName)
    {
        File directory = new File(dirName);
        List<File> fileList = getFilesRecursively(directory, classDirFilter);
        boolean foundRandFinal = false;

        for (final File f : fileList)
        {
            foundRandFinal |= checkClass(f);
        }

        return foundRandFinal;
    }

    public static void main(String[] args)
    {
        FinalRandFinder instance = new FinalRandFinder();

        boolean foundRandFinal = false;
        for (final String dirName : args)
        {
            foundRandFinal |= instance.checkDir(dirName);
        }
        if (foundRandFinal)
        {
            System.exit(1);
        }
        else
        {
            System.out.println("No @Rand/@Randc final fields found");
        }
    }
}
