As of gplcver-2.11a, cver contains an issue with its VPI implementation that
prevents pli4j from working properly. Below are instructions on how to create
a patched cver that works with pli4j.

The following instructions assume you have the environment variable CVER_HOME
set to the root of your cver installation (i.e. ${CVER_HOME}/bin exists).

Note for cygwin users: cver is very easy to build under cygwin. When
installing cygwin, make sure to install the gcc, make, and patch packages. 

Downloading cver
----------------
The GPL version of cver can be downloaded at:
http://www.pragmatic-c.com/gpl-cver/download.htm

You'll want to download a distribution that includes source code.


Unpacking cver
--------------
Unpack cver to a convenient location and set the CVER_HOME environment
variable to the root of the cver installation.


Patching cver
---------------
If you're using a source distribution of jove, the cver patch can be found at:
PATCHFILE_DIR = ${JOVE_HOME}/java/jove/docs/faq

If you're using a binary distribution of jove, the cver patch can be found at:
PATCHFILE_DIR = ${JOVE_HOME}/jove-dist-1.0/docs/jove-faq

cd ${CVER_HOME}
patch -p1 < ${PATCHFILE_DIR}/cver-2.11a-startofsimtime.patch 

cd src
# ${OS} is one of lnx, osx, or cygwin, depending on your operating system
# This command will compile a patched cver executable and copy it to the 
# ${CVER_HOME}/bin directory
make -f makefile.${OS} all


That's it! You now have a version of cver that is compatible with pli4j. If
you are running cygwin on win32, please read the file named README-WIN32.txt
in this directory.
