#!/bin/bash

buildDir=build

trap "echo -e 'To cleanup, type: \nrm -rf $buildDir; git clean -f'" EXIT

set -u
set -e

mkdir -p build
export JOVE_CLONE=$(pwd)

# Check Java 1.5.
# We can't download it without a login and agreeing to a license
javaVersion=$($JAVA_HOME/bin/java -version |& head -n 1 | awk '{print $3}')
if [[ ! $javaVersion == \"1.5*\" ]]; then
    echo "JAVA_HOME is $JAVA_HOME and that is not pointing to Java 1.5. You must use Java 1.5." >&2
    echo "Please visit https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html" >&2
    exit 1
fi

# Install Ant 1.6.5
cd $buildDir
curl -L -X GET https://archive.apache.org/dist/ant/binaries/apache-ant-1.6.5-bin.zip -o apache-ant-1.6.5-bin.zip
unzip apache-ant-1.6.5-bin.zip
export ANT_HOME=$(pwd)/apache-ant-1.6.5

# Install JavaCC
git clone https://github.com/javacc/javacc.git
cd javacc
git checkout release_40 && git switch -c 4.0
$ANT_HOME/bin/ant
export JAVACC_HOME=$(pwd)/bin/lib

# Get JUnit
cd $JOVE_CLONE/$buildDir
curl -L -X GET "https://search.maven.org/remotecontent?filepath=junit/junit/4.13.2/junit-4.13.2.jar" -o junit.jar
mkdir junit && mv junit.jar junit
export NEWISYS_HOME=$(pwd)

# Build Jove
cd $JOVE_CLONE/java
$ANT_HOME/bin/ant

# Test the randsolver
cd $JOVE_CLONE/java/randsolver
$ANT_HOME/bin/ant test

# Run the behavioral example
cd $JOVE_CLONE/java/jove-samples
make behavioral

