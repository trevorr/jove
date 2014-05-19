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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.newisys.dv.ifgen.parser.IfgenParser;
import com.newisys.dv.ifgen.parser.ParseException;
import com.newisys.dv.ifgen.schema.*;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.java.JavaRawStructuredType;
import com.newisys.langschema.java.JavaSchema;
import com.newisys.schemaprinter.java.JavaSchemaPrinter;

/**
 * Jove interface generator.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public class Ifgen
{
    private final long TS_THRESHOLD = 2;

    private final File srcRootDir;
    private Set<File> srcFiles = new LinkedHashSet<File>();
    private File timestampFile;
    private File listFile;
    private boolean forceDefaultClockPort;
    private boolean genshell;
    private Set<File> outputFiles = new LinkedHashSet<File>();
    Map<String, Set<ShellDefinition>> shellsToGenerate = new HashMap<String, Set<ShellDefinition>>();

    private class ShellDefinition
    {
        final String testbench;
        final String shellname;
        final Map<String, String> paramMap = new HashMap<String, String>();
        Map<String, Object> resolvedParamMap = null;
        Map<String, String> shellnameParams = null;

        public ShellDefinition(
            String testbench,
            Map<String, String> paramMap,
            String shellname)
        {
            this.testbench = testbench;
            this.paramMap.putAll(paramMap);
            this.shellname = shellname;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            else if (o instanceof ShellDefinition)
            {
                ShellDefinition other = (ShellDefinition) o;

                // shellname might be null. other values are never null
                boolean shellnamesMatch = this.shellname == other.shellname
                    || (this.shellname == null) ? false : this.shellname
                    .equals(other.shellname);

                return (this.testbench.equals(other.testbench))
                    && (this.paramMap.equals(other.paramMap))
                    && shellnamesMatch;
            }
            else
            {
                return false;
            }
        }

        @Override
        public int hashCode()
        {
            // shellname might be null. other values are never null

            int code = 13;
            code += 31 * testbench.hashCode();
            code += 31 * paramMap.hashCode();
            code += 31 * ((shellname == null) ? 0 : shellname.hashCode());

            return code;
        }
    }

    private class ParameterMismatchException
        extends RuntimeException
    {
        public ParameterMismatchException(String s)
        {
            super(s);
        }
    }

    public Ifgen(File srcRootDir)
    {
        this.srcRootDir = srcRootDir;
    }

    public File getSrcRootDir()
    {
        return srcRootDir;
    }

    public Set<File> getSrcFiles()
    {
        return srcFiles;
    }

    public void addSrcFile(File srcFile)
    {
        srcFiles.add(srcFile);
    }

    public File getTimestampFile()
    {
        return timestampFile;
    }

    public void setTimestampFile(File timestampFile)
    {
        this.timestampFile = timestampFile;
    }

    public File getListFile()
    {
        return listFile;
    }

    public void setListFile(File listFile)
    {
        this.listFile = listFile;
    }

    public boolean isForceDefaultClockPort()
    {
        return forceDefaultClockPort;
    }

    public void setForceDefaultClockPort(boolean forceDefaultClockPort)
    {
        this.forceDefaultClockPort = forceDefaultClockPort;
    }

    public boolean isGenshell()
    {
        return this.genshell;
    }

    public void setGenshell(boolean genshell)
    {
        this.genshell = genshell;
    }

    public Set<File> getOutputFiles()
    {
        return outputFiles;
    }

    public void addShell(
        String testbench,
        Map<String, String> paramMap,
        String shellname)
    {
        Set<ShellDefinition> shells = shellsToGenerate.get(testbench);
        if (shells == null)
        {
            shells = new LinkedHashSet<ShellDefinition>();
        }

        shells.add(new ShellDefinition(testbench, paramMap, shellname));
        shellsToGenerate.put(testbench, shells);
    }

    public void execute()
        throws ParseException, IfgenResolverException,
        IfgenTranslatorException, IOException
    {
        // reset outputs
        outputFiles.clear();

        if (srcFiles.isEmpty())
        {
            throw new IfgenTranslatorException("No source files specified");
        }

        System.out.println("Working directory: "
            + System.getProperty("user.dir"));

        // check existing timestamp file for added/removed/modified input files
        boolean needTranslation = true;
        tsCheck: if (timestampFile != null)
        {
            System.out.println("Checking timestamp file...");
            final FileReader tsFileReader;
            final long tsModified;
            try
            {
                tsFileReader = new FileReader(timestampFile);
                tsModified = timestampFile.lastModified();
                assert (tsModified > 0);
            }
            catch (FileNotFoundException e)
            {
                // timestamp file does not exist yet
                System.out.println("Timestamp file not found: "
                    + e.getMessage());
                break tsCheck;
            }
            final File baseDir = timestampFile.getParentFile()
                .getCanonicalFile();
            final Set<File> tsSrcFiles = new LinkedHashSet<File>();
            final BufferedReader tsReader = new BufferedReader(tsFileReader);
            try
            {
                String tsLine;
                while ((tsLine = tsReader.readLine()) != null)
                {
                    if (tsLine.length() < 2)
                    {
                        System.err.println("Invalid timestamp file");
                        break tsCheck;
                    }
                    char dirChar = tsLine.charAt(0);
                    final File tsFile = resolvePath(tsLine.substring(1),
                        baseDir);
                    if (dirChar == '<')
                    {
                        final long tsFileModified = tsFile.lastModified();
                        if (tsFileModified == 0
                            || tsFileModified - tsModified > TS_THRESHOLD)
                        {
                            // source file has been deleted or modified
                            System.out
                                .println("Timestamp input file has been deleted or modified: "
                                    + tsFile.getPath());
                            break tsCheck;
                        }
                        tsSrcFiles.add(tsFile);
                    }
                    else if (dirChar == '>')
                    {
                        if (!tsFile.exists())
                        {
                            // output file is missing
                            System.out
                                .println("Timestamp output file does not exist: "
                                    + tsFile.getPath());
                            break tsCheck;
                        }
                    }
                    else
                    {
                        System.err.println("Invalid timestamp file");
                        break tsCheck;
                    }
                }
            }
            finally
            {
                tsReader.close();
            }
            if (tsSrcFiles.equals(srcFiles))
            {
                // source file set is unchanged since last run
                System.out.println("Source files unchanged since last run");
                if (isGenshell() || !shellsToGenerate.isEmpty())
                {
                    // If we want to write a shell, we need to keep going.
                    needTranslation = false;
                    break tsCheck;
                }
                return;
            }
            else
            {
                System.out
                    .println("Source file set is different than last run");
            }
        }

        // delete all previously generated files if not writing the shell
        if (needTranslation)
        {
            listCheck: if (listFile != null)
            {
                BufferedReader listReader = null;
                try
                {
                    listReader = new BufferedReader(new FileReader(listFile));
                }
                catch (FileNotFoundException e)
                {
                    break listCheck;
                }
                System.out.println("Removing previously generated files");
                String outputFileName;
                while ((outputFileName = listReader.readLine()) != null)
                {
                    File f = new File(outputFileName);
                    if (f.exists() && !f.delete())
                    {
                        System.err.println("Error removing file: "
                            + f.getPath());
                    }
                }
            }
        }

        // parse all source files
        IfgenSchema ifSchema = new IfgenSchema();
        Collection<IfgenFile> files = new LinkedList<IfgenFile>();
        for (File srcFile : srcFiles)
        {
            System.out.println("Reading " + srcFile.getPath());
            IfgenFile file = parseFile(ifSchema, srcFile);
            files.add(file);
        }

        // perform name resolution and semantic checking
        IfgenResolver.resolve(ifSchema, files);

        if (needTranslation)
        {
            // generate Java schema and Verilog shells from Ifgen schema
            final JavaSchema javaSchema = new JavaSchema();
            final IfgenJavaTranslator javaXlat = new IfgenJavaTranslator(
                javaSchema);
            for (final IfgenFile file : files)
            {
                final IfgenSchemaMemberVisitor fileMemberXlat = new IfgenSchemaMemberVisitor()
                {
                    public void visit(IfgenBind obj)
                    {
                        try
                        {
                            System.out.println("Translating bind "
                                + obj.getName());
                            javaXlat.translateBind(obj);
                        }
                        catch (IfgenTranslatorException e)
                        {
                            throw new TunnelledTranslatorException(e);
                        }
                    }

                    public void visit(IfgenEnum obj)
                    {
                        System.out.println("Translating enum " + obj.getName());
                        javaXlat.translateEnum(obj);
                    }

                    public void visit(IfgenInterface obj)
                    {
                        System.out.println("Translating interface "
                            + obj.getName());
                        javaXlat.translateInterface(obj);
                    }

                    public void visit(IfgenPackage obj)
                    {
                        // not present in file
                    }

                    public void visit(IfgenPort obj)
                    {
                        System.out.println("Translating port " + obj.getName());
                        javaXlat.translatePort(obj);

                        IfgenPort revPort = obj.getReverseOf();
                        if (revPort != null)
                        {
                            System.out.println("Translating reverse port "
                                + revPort.getName());
                            javaXlat.translatePort(revPort);
                        }
                    }

                    public void visit(IfgenHDLTask obj)
                    {
                        System.out.println("Translating hdl_task "
                            + obj.getName());
                        javaXlat.translateHDLTask(obj);
                    }

                    public void visit(IfgenHVLTask obj)
                    {
                        System.out.println("Translating hvl_task "
                            + obj.getName());
                        javaXlat.translateHVLTask(obj);
                    }

                    public void visit(IfgenTestbench obj)
                    {
                        System.out.println("Translating testbench "
                            + obj.getName());
                        try
                        {
                            javaXlat.translateTestbench(obj);
                        }
                        catch (IfgenTranslatorException e)
                        {
                            throw new TunnelledTranslatorException(e);
                        }
                    }
                };

                for (IfgenSchemaMember def : file.getDefinitions())
                {
                    try
                    {
                        def.accept(fileMemberXlat);
                    }
                    catch (TunnelledTranslatorException e)
                    {
                        throw e.getCause();
                    }
                    catch (TunnelledIOException e)
                    {
                        throw e.getCause();
                    }
                }
            }

            // write generated Java source files
            final JavaSchemaPrinter printer = new JavaSchemaPrinter();
            final Set<JavaRawStructuredType> xlatTypes = javaXlat
                .getXlatTypes();
            for (JavaRawStructuredType type : xlatTypes)
            {
                File typeFile = printer.getTypeFile(type, srcRootDir);
                outputFiles.add(typeFile);
                printer.print(type, srcRootDir);
            }

            // write new timestamp file
            if (timestampFile != null)
            {
                System.out.println("Writing timestamp file: "
                    + timestampFile.getPath());
                BufferedWriter tsWriter = null;
                try
                {
                    final String timestampPath = timestampFile
                        .getCanonicalFile().getParent();
                    final FileWriter tsFileWriter = new FileWriter(
                        timestampFile);
                    tsWriter = new BufferedWriter(tsFileWriter);
                    for (File srcFile : srcFiles)
                    {
                        String srcPath = srcFile.getCanonicalPath();
                        srcPath = relativizePath(srcPath, timestampPath);
                        tsWriter.write('<');
                        tsWriter.write(srcPath);
                        tsWriter.newLine();
                    }
                    for (File outputFile : outputFiles)
                    {
                        String outputPath = outputFile.getCanonicalPath();
                        outputPath = relativizePath(outputPath, timestampPath);
                        tsWriter.write('>');
                        tsWriter.write(outputPath);
                        tsWriter.newLine();
                    }
                }
                catch (IOException e)
                {
                    // non-fatal error
                    System.err.println("Error writing timestamp file: "
                        + e.getMessage());
                }
                finally
                {
                    if (tsWriter != null) tsWriter.close();
                }
            }

            // write output file list
            if (listFile != null)
            {
                System.out.println("Writing generated file list: "
                    + listFile.getPath());
                BufferedWriter listWriter = null;
                try
                {
                    final String listPath = listFile.getCanonicalFile()
                        .getParent();
                    final FileWriter listFileWriter = new FileWriter(listFile);
                    listWriter = new BufferedWriter(listFileWriter);
                    for (File outputFile : outputFiles)
                    {
                        String outputPath = outputFile.getCanonicalPath();
                        outputPath = relativizePath(outputPath, listPath);
                        listWriter.write(outputPath);
                        listWriter.newLine();
                    }
                }
                catch (IOException e)
                {
                    // non-fatal error
                    System.err.println("Error writing generated file list: "
                        + e.getMessage());
                }
                finally
                {
                    if (listWriter != null) listWriter.close();
                }
            }
        }

        if (isGenshell() || !shellsToGenerate.isEmpty())
        {
            // generate Java schema and Verilog shells from Ifgen schema
            for (final IfgenFile file : files)
            {
                final IfgenSchemaMemberVisitor shellWriterVisitor = new IfgenSchemaMemberVisitor()
                {

                    public void visit(IfgenBind obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenEnum obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenInterface obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenPackage obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenPort obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenHDLTask obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenHVLTask obj)
                    {
                        // do nothing
                    }

                    public void visit(IfgenTestbench obj)
                    {
                        final String tbName = obj.getName().getCanonicalName();

                        try
                        {
                            // generate the shell if either 1 or 2 are satisfied
                            // 1. Non-parameterized testbench
                            //    a. genshell is true
                            //    b. the testbench wasn't explicitly named in a
                            //       -shell argument
                            //    c. the testbench has no parameters
                            // 2. Specified testbench
                            //    a. the testbench was explicitly named in a
                            //       -shell argument

                            if (isGenshell()
                                && !shellsToGenerate.containsKey(tbName)
                                && obj.getParameters().isEmpty())
                            {
                                // generate non-parameterized shell
                                generateShell(obj, file, null);
                            }
                            else
                            {
                                // generate parameterized shell (or non-parameterized
                                // shell that overrides the shellname
                                for (final String testbench : shellsToGenerate
                                    .keySet())
                                {
                                    Set<ShellDefinition> shells = shellsToGenerate
                                        .get(testbench);
                                    for (final ShellDefinition def : shells)
                                    {
                                        if (tbName.equals(def.testbench))
                                        {
                                            generateShell(obj, file, def);
                                        }
                                    }
                                }
                            }
                        }
                        catch (IOException e)
                        {
                            throw new TunnelledIOException(e);
                        }
                    }
                };

                for (IfgenSchemaMember def : file.getDefinitions())
                {
                    try
                    {
                        def.accept(shellWriterVisitor);
                    }
                    catch (TunnelledTranslatorException e)
                    {
                        throw e.getCause();
                    }
                    catch (TunnelledIOException e)
                    {
                        throw e.getCause();
                    }
                }

            }
        }
    }

    private static IfgenFile parseFile(IfgenSchema ifSchema, File srcFile)
        throws FileNotFoundException, ParseException
    {
        FileReader reader = new FileReader(srcFile);
        IfgenParser parser = new IfgenParser(reader);
        parser.setSchema(ifSchema);
        IfgenFile file = parser.file();
        file.setSourceFile(srcFile);
        return file;
    }

    private String getShellName(
        IfgenFile file,
        IfgenTestbench tb,
        ShellDefinition def)
    {

        final String tbIdentifier = getBaseName(file.getSourceFile());
        if (def != null)
        {
            if (def.shellname != null)
            {
                return def.shellname;
            }
            else
            {
                // generate a name based on the testbench name and
                // resolved parameter vals
                StringBuilder buf = new StringBuilder(64);
                buf.append(tbIdentifier);
                buf.append("_");

                for (final IfgenVariableDecl param : tb.getParameters())
                {
                    final String value = def.shellnameParams.get(param
                        .getName().getIdentifier());
                    buf.append(value);
                    buf.append("_");
                }

                buf.append("shell.v");
                return buf.toString();
            }
        }
        else
        {
            return tbIdentifier + "_shell.v";
        }
    }

    private String unquote(final String s)
    {
        // if a string is enclosed in quotes, remove the enclosing quotes.
        // also, if the string includes an escaped quote, unescape it
        String quoteChars[] = { "\"", "\'" };

        String unquoted = s;
        for (final String q : quoteChars)
        {
            if (unquoted.startsWith(q))
            {
                if (!unquoted.endsWith(q))
                {
                    throw new IllegalArgumentException("Unbalanced quotes: "
                        + s);
                }

                unquoted = unquoted.substring(1, unquoted.length() - 1);

                // does string contain any unescaped quotes? if so, error
                if (unquoted.matches(".*[^\\\\]" + q + ".*"))
                {
                    throw new IllegalArgumentException("Unbalanced quotes: "
                        + unquoted);
                }

                return unquoted.replace("\\" + q, q);
            }
        }

        return unquoted;
    }

    private String escapeStringParam(final String s)
    {
        // convert a parameter string to a string suitable for a filename
        // this means removing all quotes and changing spaces to dashes
        return s.replaceAll("\\s", "-").replaceAll("[\"\']", "");
    }

    private void writeShell(
        IfgenShellBuilder builder,
        IfgenFile file,
        String shellName)
        throws IOException
    {
        // get shell filename: <srcroot>/<pkgs>/<ifname>_shell.v
        File pkgDir = srcRootDir;
        final IfgenPackage pkg = file.getPackageDecl();
        if (pkg != null)
        {
            final String pkgName = pkg.getName().getCanonicalName();
            pkgDir = new File(pkgDir, pkgName.replace('.', File.separatorChar));
        }
        pkgDir.mkdirs();

        final File shellFile = new File(pkgDir, shellName);

        // write shell file
        outputFiles.add(shellFile);
        final String shellPath = shellFile.getPath();
        System.out.println("Writing Verilog shell: " + shellPath);
        builder.writeTo(shellPath);
    }

    void generateShell(IfgenTestbench tb, IfgenFile file, ShellDefinition def)
        throws IOException
    {
        final String moduleName = tb.getName().getIdentifier();
        final IfgenShellBuilder builder = new IfgenShellBuilder(moduleName);
        if (forceDefaultClockPort) builder.setNeedDefaultClock(true);

        Map<String, Object> params = initializeValueMap(tb, def);
        addShellMembers(params, new HashSet<IfgenSchemaMember>(), tb, builder);
        writeShell(builder, file, getShellName(file, tb, def));

    }

    private Map<String, Object> initializeValueMap(
        IfgenTestbench tb,
        ShellDefinition def)
    {
        // initialize the value map with the testbench parameters (if any)
        final Map<String, Object> valueMap = new HashMap<String, Object>();
        final Map<String, String> defMap = new HashMap<String, String>();
        final Map<String, String> shellnameMap = new HashMap<String, String>();

        if (def != null)
        {
            defMap.putAll(def.paramMap);
        }

        for (final IfgenVariableDecl decl : tb.getParameters())
        {
            final String argName = decl.getName().getIdentifier();
            final IfgenType argType = decl.getType();
            final String argValue = defMap.get(argName);
            defMap.remove(argName);
            final IfgenSchema ifSchema = tb.getSchema();
            if (argValue == null)
            {
                throw new ParameterMismatchException("Testbench parameter '"
                    + argName + "' must be defined");
            }
            if (argType == ifSchema.INTEGER_TYPE)
            {
                valueMap.put(argName, Integer.parseInt(argValue));
                shellnameMap.put(argName, argValue);
            }
            else if (argType == ifSchema.STRING_TYPE)
            {

                valueMap.put(argName, unquote(argValue));
                shellnameMap.put(argName, escapeStringParam(unquote(argValue)));
            }
            else if (argType instanceof IfgenEnumType)
            {
                IfgenEnum enumeration = ((IfgenEnumType) argType)
                    .getEnumeration();
                Iterator< ? extends NamedObject> eIter = enumeration
                    .lookupObjects(argValue, IfgenNameKind.EXPRESSION);
                assert (eIter.hasNext());
                IfgenEnumElement elt = (IfgenEnumElement) eIter.next();
                assert (!eIter.hasNext());
                valueMap.put(argName, elt);
                shellnameMap.put(argName, argValue);
            }
            else
            {
                assert (argType instanceof IfgenSetType);
                final IfgenType memberType = ((IfgenSetType) argType)
                    .getMemberType();
                final Set<Object> set = new TreeSet<Object>();

                if (!argValue.startsWith("["))
                {
                    throw new ParameterMismatchException(
                        "Set value must start with '[': " + argValue);
                }
                else if (!argValue.endsWith("]"))
                {
                    throw new ParameterMismatchException(
                        "Set value must end with ']': " + argValue);
                }
                final String[] members = argValue.substring(1,
                    argValue.length() - 1).split("\\s");

                final StringBuilder buf = new StringBuilder(64);
                for (int i = 0; i < members.length; ++i)
                {
                    final String s = members[i].trim();
                    if (s.length() == 0) continue;

                    if (memberType == ifSchema.INTEGER_TYPE)
                    {
                        set.add(Integer.parseInt(s));
                        buf.append(s);
                    }
                    else if (memberType == ifSchema.STRING_TYPE)
                    {
                        set.add(unquote(s));
                        buf.append(escapeStringParam(unquote(s)));
                    }
                    else if (memberType instanceof IfgenEnumType)
                    {
                        IfgenEnum enumeration = ((IfgenEnumType) memberType)
                            .getEnumeration();
                        Iterator< ? extends NamedObject> eIter = enumeration
                            .lookupObjects(s, IfgenNameKind.EXPRESSION);
                        assert (eIter.hasNext());
                        IfgenEnumElement elt = (IfgenEnumElement) eIter.next();
                        assert (!eIter.hasNext());
                        set.add(elt);
                        buf.append(s);
                    }

                    if (i < (members.length - 1))
                    {
                        buf.append("-");
                    }
                }
                valueMap.put(argName, set);
                shellnameMap.put(argName, buf.toString());
            }

        }

        if (!defMap.isEmpty())
        {
            // User supplied parameters that are not used by the testbench.
            // throw an exception and tell them which parameters are wrong
            StringBuilder buf = new StringBuilder(64);
            Iterator<String> iter = defMap.keySet().iterator();
            while (iter.hasNext())
            {
                buf.append(iter.next());
                if (iter.hasNext())
                {
                    buf.append(", ");
                }
            }

            throw new ParameterMismatchException(
                "Defined parameter(s) are not testbench paramters: [" + buf
                    + "]");
        }

        for (final String s : valueMap.keySet())
        {
            System.out.println(s + " => " + valueMap.get(s));
        }

        // if the shell definition is valid, set its resolvedParamMap to be
        // the String->Object mapping generated by this method. Further, set
        // its shellnameParams member to be the String->String map generated by
        // this method.
        if (def != null)
        {
            def.shellnameParams = shellnameMap;
            def.resolvedParamMap = valueMap;
        }

        return valueMap;
    }

    private void addShellMembers(
        Map<String, Object> valueMap,
        Set<IfgenSchemaMember> addedSet,
        IfgenTestbenchMemberContainer container,
        IfgenShellBuilder builder)
    {
        for (IfgenTestbenchMember member : container.getMembers())
        {
            if (addedSet.contains(member)) continue;

            if (member instanceof IfgenForStatement)
            {
                IfgenForStatement forStmt = (IfgenForStatement) member;
                IfgenVariableDecl decl = forStmt.getVarDecl();
                IfgenExpression expr = forStmt.getSet();
                assert (expr.getType() instanceof IfgenSetType);
                IfgenType setType = ((IfgenSetType) expr.getType())
                    .getMemberType();
                assert (setType == decl.getType());

                Set< ? > setValues = evaluateSetExpr(expr, valueMap);
                for (Object o : setValues)
                {
                    Map<String, Object> scopedMap = new HashMap<String, Object>(
                        valueMap);
                    scopedMap.put(decl.getName().getIdentifier(), o);
                    addShellMembers(scopedMap, addedSet, forStmt, builder);
                }
            }
            else if (member instanceof IfgenHVLTask)
            {
                IfgenHVLTask hvlTask = (IfgenHVLTask) member;
                builder.addHVLTask(hvlTask);
                addedSet.add(hvlTask);
            }
            else if (member instanceof IfgenHDLTask)
            {
                IfgenHDLTask hdlTask = (IfgenHDLTask) member;
                if (!hdlTask.hasParameters())
                {
                	builder.addHDLTask(hdlTask, hdlTask.getName().getIdentifier(), hdlTask.getInstancePath().getName().toString());
                }
                addedSet.add(hdlTask);
            }
            else if (member instanceof IfgenInterface)
            {
                IfgenInterface intf = (IfgenInterface) member;
                if (!intf.hasParameters())
                {
                    builder.addInterface(intf);
                }
                addedSet.add(intf);
            }
            else if (member instanceof IfgenTemplateInst)
            {
                IfgenTemplateInst inst = (IfgenTemplateInst) member;
                Map<String, Object> paramMap = makeParamMap(inst, valueMap);

                StringBuilder buf = new StringBuilder(128);
                for (final String key : paramMap.keySet())
                {
                    if (buf.length() > 0)
                    {
                        buf.append("_");
                    }
                    Object value = paramMap.get(key);
                    assert (!(value instanceof Set));
                    if (value instanceof IfgenEnumElement)
                    {
                        buf.append(((IfgenEnumElement) value).getName()
                            .getIdentifier());
                    }
                    else
                    {
                        buf.append(value);
                    }
                }
                final String identifier = buf.toString();

                switch (inst.getTemplateKind())
                {
                case INTERFACE:
                    // evaluate any default modules or signals using variables
                    builder.addInterface(createInterfaceInstance(inst,
                        identifier, paramMap));
                    break;
                case HDL_TASK:
                    IfgenHDLTask task = (IfgenHDLTask) inst.getTemplate();
                    builder.instantiateHDLTask(inst, paramMap, getInstancePath(task, paramMap));
                    break;
                default:
                // do nothing
                }
            }
            else
            {
                throw new AssertionError("Unexpected shell member type: "
                    + member.getClass().getName());
            }
        }
    }

    private String getInstancePath(IfgenHDLTask task, Map<String, Object> valueMap)
    {
        IfgenModuleDef module = task.getInstancePath();
        final String path;
        if (module.containsVarRef())
        {
            IfgenComplexVariableRef ref = module.getComplexRef();
            path = (String) evaluateExpr(ref, valueMap);            
        }
        else
        {
            path = module.getName().toString();
        }
        return path;
    }
    
    private IfgenInterface createInterfaceInstance(
        IfgenTemplateInst inst,
        String identifier,
        Map<String, Object> valueMap)
    {
        IfgenInterface intf = (IfgenInterface) inst.getTemplate();

        IfgenName oldName = intf.getName();
        IfgenName newName = new IfgenName(oldName.getIdentifier() + "_"
            + identifier, oldName.getKind(), oldName.getNamespace());
        IfgenInterface newIntf = new IfgenInterface(intf.getSchema(), newName,
            intf.getParameters());

        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenInterfaceSignal)
            {
                IfgenInterfaceSignal oldSignal = (IfgenInterfaceSignal) member;
                IfgenInterfaceSignal signal = oldSignal.makeCopy(newIntf);
                IfgenModuleDef module = signal.getModule();

                // Need to evaluate modules that are complex or signals whose
                // paths are complex
                // Module check:
                if (module != null && module.containsVarRef())
                {
                    IfgenComplexVariableRef ref = module.getComplexRef();
                    final String path = (String) evaluateExpr(ref, valueMap);
                    final String signalPath = path + "."
                        + signal.getName().getIdentifier();
                    signal.setEffectiveHDLNode(new IfgenHDLSignalRef(intf
                        .getSchema(), signalPath));
                    signal.setModule(new IfgenModuleDef(module.getSchema(),
                        new IfgenUnresolvedName(path), false));
                }

                // HDL node check:
                IfgenSignalRef hdlNode = signal.getEffectiveHDLNode();
                if (hdlNode instanceof IfgenHDLSignalRef)
                {
                    IfgenHDLSignalRef ref = (IfgenHDLSignalRef) hdlNode;
                    if (ref.getHDL() instanceof IfgenComplexVariableRef)
                    {
                        final String path = (String) evaluateExpr(ref.getHDL(),
                            valueMap);

                        signal.setEffectiveHDLNode(new IfgenHDLSignalRef(inst
                            .getSchema(), path));
                    }
                }

                newIntf.addMember(signal);
            }
            else
            {
                newIntf.addMember(member);
            }
        }

        return newIntf;
    }

    private Map<String, Object> makeParamMap(
        IfgenTemplateInst inst,
        Map<String, Object> valueMap)
    {
        List<IfgenExpression> args = inst.getArgs();
        List<IfgenVariableDecl> params = inst.getTemplate().getParameters();
        assert (args.size() == params.size());

        Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
        for (int i = 0; i < params.size(); ++i)
        {
            final String name = params.get(i).getName().getIdentifier();
            final Object obj = evaluateExpr(args.get(i), valueMap);
            paramMap.put(name, obj);
        }

        return paramMap;
    }

    private Set< ? > evaluateSetExpr(
        IfgenExpression expr,
        Map<String, Object> valueMap)
    {
        Object obj = evaluateExpr(expr, valueMap);
        if (obj instanceof Set)
        {
            return (Set) obj;
        }
        else
        {
            Set<Object> set = new HashSet<Object>();
            set.add(obj);
            return set;
        }
    }

    private Object evaluateExpr(
        IfgenExpression expr,
        Map<String, Object> valueMap)
    {
        if (expr instanceof IfgenIntegerLiteral)
        {
            IfgenIntegerLiteral literal = (IfgenIntegerLiteral) expr;
            return literal.getValue();
        }
        else if (expr instanceof IfgenStringLiteral)
        {
            IfgenStringLiteral literal = (IfgenStringLiteral) expr;
            return literal.getString();
        }
        else if (expr instanceof IfgenEnumLiteral)
        {
            IfgenEnumLiteral literal = (IfgenEnumLiteral) expr;
            return literal.getElement();
        }
        else if (expr instanceof IfgenVariableRef)
        {
            IfgenVariableRef ref = (IfgenVariableRef) expr;
            Object obj = valueMap.get(ref.getName().getIdentifier());
            if (obj == null)
            {
                throw new Error("Undefined variable: " + ref);
            }
            return obj;
        }
        else if (expr instanceof IfgenComplexVariableRef)
        {
            // Complex variable refs always result in strings
            IfgenComplexVariableRef ref = (IfgenComplexVariableRef) expr;
            StringBuilder buf = new StringBuilder(128);
            for (final IfgenExpression chunk : ref.getExpressions())
            {
                Object obj = evaluateExpr(chunk, valueMap);
                assert (!(obj instanceof Set));
                if (obj instanceof IfgenEnumElement)
                {
                    buf.append(((IfgenEnumElement) obj).getName()
                        .getIdentifier());
                }
                else
                {
                    buf.append(obj);
                }
            }
            return buf.toString();
        }
        else if (expr instanceof IfgenSetLiteral)
        {
            Set<Object> set = new LinkedHashSet<Object>();
            IfgenSetLiteral lit = (IfgenSetLiteral) expr;
            IfgenSet s = lit.getSet();
            for (IfgenExpression e : s.getExpressions())
            {
                assert (!(e.getType() instanceof IfgenSetType));
                set.add(evaluateExpr(e, valueMap));
            }
            for (IfgenRange r : s.getRanges())
            {
                // ranges must be of type int or an enum type
                if (r.getType() == expr.getSchema().INTEGER_TYPE)
                {
                    Object fromObj = evaluateExpr(r.getFromExpr(), valueMap);
                    Object toObj = evaluateExpr(r.getToExpr(), valueMap);
                    assert (fromObj.getClass() == Integer.class);
                    assert (toObj.getClass() == Integer.class);

                    int fromValue = (Integer) fromObj;
                    int toValue = (Integer) toObj;
                    int minValue = Math.min(fromValue, toValue);
                    int maxValue = Math.max(fromValue, toValue);
                    for (int i = minValue; i <= maxValue; ++i)
                    {
                        set.add(i);
                    }
                }
                else if (r.getType() instanceof IfgenEnumType)
                {
                    Object fromObj = evaluateExpr(r.getFromExpr(), valueMap);
                    Object toObj = evaluateExpr(r.getToExpr(), valueMap);
                    assert (fromObj instanceof IfgenEnumElement);
                    assert (toObj instanceof IfgenEnumElement);

                    IfgenEnumElement from = (IfgenEnumElement) fromObj;
                    IfgenEnumElement to = (IfgenEnumElement) toObj;
                    IfgenEnum enumeration = from.getEnumType().getEnumeration();

                    List<IfgenEnumElement> range = enumeration.getRange(from,
                        to);
                    assert set.isEmpty();
                    for (IfgenEnumElement e : range)
                    {
                        set.add(e);
                    }
                }
                else
                {
                    throw new Error("Illegal range type '" + r.getType()
                        + "'. Must be integer or enumeration");
                }
            }
            return set;
        }
        else if (expr instanceof IfgenSetIntersection)
        {
            IfgenSetOperator setOp = (IfgenSetOperator) expr;
            Set< ? > lhsSet = evaluateSetExpr(setOp.getLHS(), valueMap);
            Set< ? > rhsSet = evaluateSetExpr(setOp.getRHS(), valueMap);

            Set<Object> intersectionSet = new HashSet<Object>(lhsSet);
            intersectionSet.retainAll(rhsSet);
            return intersectionSet;
        }
        else if (expr instanceof IfgenSetDifference)
        {
            IfgenSetOperator setOp = (IfgenSetOperator) expr;
            Set< ? > lhsSet = evaluateSetExpr(setOp.getLHS(), valueMap);
            Set< ? > rhsSet = evaluateSetExpr(setOp.getRHS(), valueMap);

            Set<Object> differenceSet = new HashSet<Object>(lhsSet);
            differenceSet.addAll(rhsSet);

            // get the intersection
            lhsSet.retainAll(rhsSet);
            // remove the intersection from the union (leaving the difference)
            differenceSet.removeAll(lhsSet);
            return differenceSet;
        }
        else if (expr instanceof IfgenSetUnion)
        {
            IfgenSetOperator setOp = (IfgenSetOperator) expr;
            Set< ? > lhsSet = evaluateSetExpr(setOp.getLHS(), valueMap);
            Set< ? > rhsSet = evaluateSetExpr(setOp.getRHS(), valueMap);

            Set<Object> unionSet = new HashSet<Object>(lhsSet);
            unionSet.addAll(rhsSet);
            return unionSet;
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported set expr: "
                + expr.getClass());
        }
    }

    private static String relativizePath(String path, String base)
    {
        if (path.startsWith(base))
        {
            int start = base.length();
            if (path.charAt(start) == File.separatorChar) ++start;
            path = path.substring(start);
        }
        return path;
    }

    private static File resolvePath(String path, File base)
    {
        File f = new File(path);
        if (!f.isAbsolute()) f = new File(base, path);
        return f;
    }

    private static String getBaseName(File file)
    {
        String name = file.getName();
        int dotPos = name.lastIndexOf('.');
        if (dotPos >= 0) name = name.substring(0, dotPos);
        return name;
    }
}
