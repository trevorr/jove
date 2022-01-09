_[NOTE: This document is based on the [Jove FAQ](http://jove.sourceforge.net/docs/jove-faq/index.html), last updated October 19, 2005. Other than translation to Markdown, it has not been substantially updated since then.]_

What is Jove?
----

Jove is a set of Java APIs and tools to enable Verilog hardware design verification using the Java programming language. It contains components that accomplish the following:

- Verilog simulator interaction (via PLI 2.0, aka VPI)
- standalone behavioral simulation (i.e. a discrete event simulator)
- thread and event synchronization
- design verification abstractions (e.g. clock-relative signal access, mailboxes, semaphores)
- constraint-based randomization
- Verilog shell generation

In other words, it provides facilities similar to Synopsys Vera, Cadence Testbuilder, and (formerly) Verisity Specman.

Why Jove?
----

Verification has become a very software-intensive task, and Jove is intended to leverage Java's strengths as a robust software engineering environment to deal with very large, complex verification codebases.

- Jove leverages the Java language, a standard, well-defined, high-productivity object-oriented programming language. Alternative tools introduce proprietary languages, or use more complex, less productive languages, like C/C++ or Perl.
- Jove leverages Java libraries, including the extensive Java runtime library, the JUnit test framework, and thousands of third-party libraries. Java libraries tend to be much easier to integrate than libraries in earlier mature languages, such as C/C++.
- Jove leverages Java tools, such as Javadoc and Eclipse. The Eclipse IDE, for instance, provides Jove users with a free, powerful GUI debugger.
- Jove leverages Java Virtual Machine technology, arguably the most mature, high-performance VM technology available, with performance rivaling compiled languages in most cases.
- Jove leverages the experience of a software-oriented team executing the complete lifecycle of a very complex ASIC verification project. It addresses the limitations encountered using other tools during that project.

What is the status of Jove?
----

Jove should be considered relatively stable and mature (though like most free software, it carries no warranties of any kind; see the [license](https://raw.githubusercontent.com/trevorr/jove/master/LICENSE-osl-2.0.txt) for details). It has been in production use for the verification of Newisys' Horus ASIC, running a verification codebase of over 400K lines of Java source, since February 2005 and has proven exceptionally stable.

What platforms does Jove support?
----

Most of Jove is written in the Java 5.0 language, and should therefore work on any platform with a Java 5.0 JDK. Specifically, Java-only behavioral simulations should work on any Java platform. However, the Verilog simulator support is written in C++ and therefore tends to need tweaking to work on new platforms. The table below lists platforms that Jove has been tested on so far.

| Operating System | Processor Architecture | Supported Simulators |
|------------------|------------------------|----------------------|
| Red Hat Enterprise Linux AS 3 | Intel x86 | Synopsys VCS, GPL Cver |
| Microsoft Windows XP + Cygwin 1.5.18-1 | Intel x86 | GPL Cver |
| Apple Mac OS X 10.4.2 + J2SE 5.0 Release 1 | PowerPC | GPL Cver |


Modernization
----

An attempt is being made to upgrade Jove to build with Gradle on Java 17. It might be interesting to write a scheduler using Java Loom.


What Verilog simulators does Jove support?
----

Jove is designed to be easily ported to any simulator supporting PLI 2.0 / VPI. It has been tested with the simulators listed in the table below. We hope that support for other simulators will be forthcoming.

| Simulator | Status | Notes |
|-----------|--------|-------|
| Synopsys VCS | Extensively tested | Jove exposed various bugs in the VCS VPI implementation, most of which have been fixed in recent versions, so using the latest version is recommended. |
| Pragmatic C GPL Cver | Minimal testing | Tested with version 2.11a, which requires a patch to fix a bug in setting Start of Sim Time callbacks. |

How is Jove licensed?
----

Jove is licensed under the [Open Software License 2.0](https://raw.githubusercontent.com/trevorr/jove/master/LICENSE-osl-2.0.txt). It also includes the following third-party software:

- PLI4J - A Java&trade; Interface to the Verilog PLI  
Copyright &copy; 2003 Trevor Robinson  
Licensed under the Academic Free License 2.0 (see LICENSE-afl-2.0.txt).
- JavaBDD  
Copyright &copy; 2003 John Whaley <jwhaley@alum.mit.edu>  
Licensed under the GNU Lesser General Public License (see LICENSE-lgpl.txt).
- Generated code from JavaCC&trade;  
Copyright &copy; 2003 Sun Microsystems, Inc. All Rights Reserved.  
Licensed under a BSD-style license (see LICENSE-javacc.txt).

Product and company names mentioned herein may be trademarks of their respective owners. Java is a trademark or registered trademark of Oracle and/or its affiliates.

Who wrote Jove?
----

Jove was created at Newisys, Inc. The project was conceived and architected by Trevor Robinson. Jon Nall implemented the random solver and BitVector components, and performed most of the bring-up and testing. Scott Diesing implemented the behavioral simulator. Mark Davis managed the project. Jove also includes some third-party software, listed above.
