Currently the only simulator supported on win32 is cver. Before compiling
pli4j, the DLL for cver needs to be built. Instructions for building the DLL
are available in the cver src distribution in
${CVER_HOME}/tests_and_examples/examples.vpi/README. However the instructions
provided below have been tested and verified to work. We suggest you follow
the instructions below.

Building the cver DLL for Win32 systems
---------------------------------------

1. Patch the cver source and build the cver binary. Please refer to
README.cver in this directory for instructions on completing this step.

2. Build the cver DLL as follows:
cd ${CVER_HOME}/objs
make -f makefile.dll dll
make -f makefile.dll exe
cp cver.exe libcver.dll ${CVER_HOME}/bin

After these steps have been completed, compiling pli4j with SIMULATOR=cver
will result in a DLL that can be loaded into cver via +loadvpi.
