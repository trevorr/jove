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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.newisys.dv.ifgen.schema.*;
import com.newisys.io.IndentWriter;

/**
 * Verilog shell builder for the Jove interface generator.
 * 
 * @author Trevor Robinson
 */
public final class IfgenShellBuilder
{
    private static class Signal
    {
        final String name;
        final IfgenDirection dir;
        final int size;
        final String hdlNode;

        public Signal(String name, IfgenDirection dir, int size, String hdlNode)
        {
            this.name = name;
            this.dir = dir;
            this.size = size;
            this.hdlNode = hdlNode;
        }
    }

    /*
    private static class HDLTaskInstInfo
    {
        Map<String, Object> paramMap;
        String instPath;
    }
    */
    
    private static class HDLTask
    {
        final String name;
        final String startName;
        final String doneName;
        final List<HDLTaskArgument> args = new LinkedList<HDLTaskArgument>();
        //final List<HDLTaskInstInfo> instInfoList = new LinkedList<HDLTaskInstInfo>();
        final String instPath;

        public HDLTask(
            String name,
            String startName,
            String doneName,
            String instPath)
        {
            this.name = name;
            this.startName = startName;
            this.doneName = doneName;
            this.instPath = instPath;
        }
    }

    private static class HDLTaskArgument
    {
        final String name;
        final int size;
        final boolean isInteger;

        public HDLTaskArgument(String name, int size)
        {
            this.name = name;
            this.size = size;
            this.isInteger = false;
        }

        public HDLTaskArgument(String name)
        {
            this.name = name;
            this.size = 32;
            this.isInteger = true;
        }

        public boolean isInteger()
        {
            return this.isInteger;
        }
    }

    private static class HVLTask
    {
        final String name;
        final String doneName;
        final List<HVLTaskArgument> args = new LinkedList<HVLTaskArgument>();

        public HVLTask(String name, String doneName)
        {
            this.name = name;
            this.doneName = doneName;
        }
    }

    private static class HVLTaskArgument
    {
        final String name;
        final IfgenDirection dir;
        final int size;

        public HVLTaskArgument(String name, IfgenDirection dir, int size)
        {
            this.name = name;
            this.dir = dir;
            this.size = size;
        }
    }

    private final String moduleName;
    private final Map<String, HDLTask> hdlTaskCache = new HashMap<String, HDLTask>();
    private final List<Signal> signals = new LinkedList<Signal>();
    private final List<HDLTask> hdlTasks = new LinkedList<HDLTask>();
    private final List<HVLTask> hvlTasks = new LinkedList<HVLTask>();
    private boolean needDefaultClock;

    public IfgenShellBuilder(String moduleName)
    {
        this.moduleName = moduleName;
    }

    public boolean isNeedDefaultClock()
    {
        return needDefaultClock;
    }

    public void setNeedDefaultClock(boolean needDefaultClock)
    {
        this.needDefaultClock = needDefaultClock;
    }

    public boolean isEmpty()
    {
        return signals.isEmpty() && hdlTasks.isEmpty() && hvlTasks.isEmpty();
    }

    public void addInterface(IfgenInterface intf)
    {
        boolean gotClock = false;
        for (IfgenInterfaceMember member : intf.getMembers())
        {
            if (member instanceof IfgenInterfaceSignal)
            {
                IfgenInterfaceSignal signal = (IfgenInterfaceSignal) member;
                String fullName = IfgenJavaTranslator.getSignalName(intf,
                    signal);
                IfgenSignalType type = signal.getType();
                int size = signal.getSize();
                IfgenSignalRef hdlNode = signal.getEffectiveHDLNode();
                String hdlNodeStr = hdlNode != null ? hdlNode.toString() : null;
                signals.add(new Signal(fullName, getSignalDirection(type),
                    size, hdlNodeStr));
                gotClock |= (type == IfgenSignalType.CLOCK);
            }
        }
        if (!gotClock) needDefaultClock = true;
    }

    private IfgenDirection getSignalDirection(IfgenSignalType type)
    {
        switch (type)
        {
        case CLOCK:
        case INPUT:
            return IfgenDirection.INPUT;
        case OUTPUT:
            return IfgenDirection.OUTPUT;
        case INOUT:
            return IfgenDirection.INOUT;
        default:
            throw new AssertionError("Unknown signal type: " + type);
        }
    }

    public void instantiateHDLTask(IfgenTemplateInst inst, Map<String, Object> paramMap, String instancePath)
    {
    	IfgenHDLTask task = (IfgenHDLTask) inst.getTemplate();
    	String nameString = task.getName().getIdentifier().toString();
    	for (Object cur : paramMap.values())
    	{
    		nameString = nameString + "_" + cur.toString();
    	}
    	nameString = nameString.trim();
    	addHDLTask(task, nameString, instancePath);
    }

    public HDLTask addHDLTask(IfgenHDLTask task, String taskID, String instancePath)
    {
        HDLTask info = hdlTaskCache.get(taskID);
        if (info == null)
        {
	        info = new HDLTask(taskID, "ifgen_start_" + taskID, "ifgen_done_"
	            + taskID, instancePath);
	
	        // normal arguments are all bit vectors
	        for (IfgenTaskArg arg : task.getArguments())
	        {
	            String argID = arg.getName().getIdentifier();
	            String argName = "ifgen_" + taskID + "_" + argID;
	            int argSize = arg.getSize();
	            info.args.add(new HDLTaskArgument(argName, argSize));
	        }
	        hdlTasks.add(info);
	        hdlTaskCache.put(taskID, info);
        }
        else
        {
	        // the only time a task should be cached is if it can be
	        // instantiated (i.e. has parameters)
	        if (!task.hasParameters())
	        {
	            throw new AssertionError("Multiply defined HDL task: "
	                + task.getName());
	        }
        }
        return info;
    }
    
    public void addHVLTask(IfgenHVLTask task)
    {
        String taskID = task.getName().getIdentifier();
        HVLTask info = new HVLTask(taskID, "ifgen_done_" + taskID);
        for (IfgenTaskArg arg : task.getArguments())
        {
            String argID = arg.getName().getIdentifier();
            IfgenDirection argDir = arg.getDirection();
            int argSize = arg.getSize();
            info.args.add(new HVLTaskArgument(argID, argDir, argSize));
        }
        hvlTasks.add(info);
    }

    public void writeTo(Writer out)
    {
        // add port for DefaultClock if necessary
        if (needDefaultClock)
        {
            signals.add(0, new Signal("DefaultClock", IfgenDirection.INPUT, 1,
                null));
            needDefaultClock = false;
        }

        final IndentWriter iw = new IndentWriter(out);
        final PrintWriter pw = new PrintWriter(iw);

        // declare module
        pw.println("module " + moduleName + "(");
        iw.incIndent();

        // declare ports
        int portCount = 0;
        for (Signal signal : signals)
        {
            if (signal.hdlNode != null) continue;

            if (portCount > 0) pw.println(',');
            pw.print(signal.name);
            ++portCount;
        }
        if (portCount > 0) pw.println();

        iw.decIndent();
        pw.println(");");
        iw.incIndent();

        // define port directions
        for (Signal signal : signals)
        {
            if (signal.hdlNode != null) continue;

            pw.print(getDirectionString(signal.dir));
            pw.print(getVectorString(signal.size));
            pw.print(' ');
            pw.print(signal.name);
            pw.println(';');
        }
        pw.println();

        // declare signal nets/regs
        pw.println("// declare signal nets/regs");
        for (Signal signal : signals)
        {
            // declare input net
            if (isInput(signal.dir))
            {
                final String inName = signal.name + "_in";

                pw.print("wire");
                pw.print(getVectorString(signal.size));
                pw.print(' ');
                pw.print(inName);
                pw.println(';');

                // define continuous assignment for input net
                pw.print("assign ");
                pw.print(inName);
                pw.print(" = ");
                if (signal.hdlNode != null)
                {
                    pw.print(signal.hdlNode);
                }
                else
                {
                    pw.print(signal.name);
                }
                pw.println(';');
            }

            // declare output reg
            if (isOutput(signal.dir))
            {
                final String outName = signal.name + "_out";

                pw.print("reg");
                pw.print(getVectorString(signal.size));
                pw.print(' ');
                pw.print(outName);
                pw.println(';');

                // define continuous assignment for output reg
                pw.print("assign ");
                if (signal.hdlNode != null)
                {
                    pw.print(signal.hdlNode);
                }
                else
                {
                    pw.print(signal.name);
                }
                pw.print(" = ");
                pw.print(outName);
                pw.println(';');
            }

            pw.println();
        }

        // define wrappers for imported HDL functions
        for (HDLTask info : hdlTasks)
        {
            // print wrapper comment
            pw.print("// ");
            pw.print(info.name);
            pw.println(" task wrapper");

            // define start/done registers
            pw.print("reg ");
            pw.print(info.startName);
            pw.print(", ");
            pw.print(info.doneName);
            pw.println(';');

            // initialize start/done registers
            pw.println("initial begin");
            iw.incIndent();
            pw.print(info.startName);
            pw.println(" = 0;");
            pw.print(info.doneName);
            pw.println(" = 0;");
            iw.decIndent();
            pw.println("end");

            for (HDLTaskArgument arg : info.args)
            {
                if (arg.isInteger())
                {
                    pw.print("integer");
                }
                else
                {
                    pw.print("reg");
                    pw.print(getVectorString(arg.size));
                }
                pw.print(' ');
                pw.print(arg.name);
                pw.println(';');
            }

            // define invocation loop
            pw.print("always @(posedge ");
            pw.print(info.startName);
            pw.println(") begin");
            iw.incIndent();
            
            pw.print(info.instPath);
            if (info.args.size() > 0)
            {
                pw.print('(');
                boolean first = true;
                for (HDLTaskArgument arg : info.args)
                {
                    if (!first) pw.print(", ");
                    pw.print(arg.name);
                    first = false;
                }
                pw.print(")");
            }
            pw.println(";");
            pw.print(info.startName);
            pw.println(" = 0;");
            pw.print(info.doneName);
            pw.println(" = 1;");
            iw.decIndent();
            pw.println("end");

            pw.println();
        }

        // define wrappers for exported HVL functions
        for (HVLTask info : hvlTasks)
        {
            // print wrapper comment
            pw.print("// ");
            pw.print(info.name);
            pw.println(" task wrapper");

            // begin task declaration
            pw.print("task ");
            pw.print(info.name);
            pw.println(';');
            iw.incIndent();

            // define arguments
            for (HVLTaskArgument arg : info.args)
            {
                pw.print(getDirectionString(arg.dir));
                pw.print(' ');
                pw.print("reg");
                pw.print(getVectorString(arg.size));
                pw.print(' ');
                pw.print(arg.name);
                pw.println(';');
            }
            pw.println();

            // define done register
            pw.print("reg ");
            pw.print(info.doneName);
            pw.println(';');

            // begin body
            pw.println("begin");
            iw.incIndent();

            // initialize done register
            pw.print(info.doneName);
            pw.println(" = 0;");

            // dispatch call
            pw.print("$pli4j_call(\"");
            pw.print(info.name);
            pw.print("\", ");
            pw.print(info.doneName);
            for (HVLTaskArgument arg : info.args)
            {
                pw.print(", ");
                pw.print(arg.name);
            }
            pw.println(");");

            // wait for done register
            pw.print("@(");
            pw.print(info.doneName);
            pw.println(");");

            // end body
            iw.decIndent();
            pw.println("end");

            // end task declaration
            iw.decIndent();
            pw.println("endtask");

            pw.println();
        }

        // define initial block
        pw.println("initial begin");
        iw.incIndent();

        // initialize output regs
        pw.println("// initialize output regs");
        for (Signal signal : signals)
        {
            if (isOutput(signal.dir))
            {
                final String outName = signal.name + "_out";
                pw.print(outName);
                pw.print(" = ");
                pw.print(signal.size);
                pw.println("'bz;");
            }
        }
        pw.println();

        // initialize pli4j
        pw.println("// initialize pli4j");
        pw.println("$pli4j_init;");
        pw.println();

        // register signal nets/regs
        pw.println("// register signal nets/regs");
        for (Signal signal : signals)
        {
            pw.print("$pli4j_register_signal(\"");
            pw.print(signal.name);
            pw.print("\", ");

            // register input net
            if (isInput(signal.dir))
            {
                final String inName = signal.name + "_in";
                pw.print(inName);
            }
            else
            {
                pw.print("0");
            }
            pw.print(", ");

            // register output reg
            if (isOutput(signal.dir))
            {
                final String outName = signal.name + "_out";
                pw.print(outName);
            }
            else
            {
                pw.print("0");
            }
            pw.println(");");
        }
        pw.println();

        // register HDL functions
        pw.println("// register HDL tasks");
        for (HDLTask info : hdlTasks)
        {
            // define invocation loop
            pw.print("$pli4j_register_verilog_task(\"");
            pw.print(info.name);
            pw.print("\", ");
            pw.print(info.startName);
            pw.print(", ");
            pw.print(info.doneName);
            
            for (HDLTaskArgument arg : info.args)
            {
                pw.print(", ");
                pw.print(arg.name);
            }
            pw.println(");");
        }
        pw.println();
                      
        // start pli4j
        pw.println("// start pli4j");
        pw.println("$pli4j_start;");

        iw.decIndent();
        pw.println("end");

        iw.decIndent();
        pw.println("endmodule");

        pw.close();
    }

    private static boolean isInput(IfgenDirection dir)
    {
        return dir == IfgenDirection.INPUT || dir == IfgenDirection.INOUT;
    }

    private static boolean isOutput(IfgenDirection dir)
    {
        return dir == IfgenDirection.OUTPUT || dir == IfgenDirection.INOUT;
    }

    private static String getDirectionString(IfgenDirection dir)
    {
        if (dir == IfgenDirection.INPUT)
        {
            return "input";
        }
        else if (dir == IfgenDirection.OUTPUT)
        {
            return "output";
        }
        return "inout";
    }

    private static String getVectorString(int size)
    {
        return size > 1 ? "[" + (size - 1) + ":0]" : "";
    }

    public void writeTo(String filename)
        throws IOException
    {
        writeTo(new FileWriter(filename));
    }
}
