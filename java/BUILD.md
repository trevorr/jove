# Build Jove - Java part

Jove is built with Ant. Ant does not have dependency management.
For this reason, dependencies must be obtained by hand.
Fortunately, there are only a few dependencies to get.

To build from source, you will need:

* [Java 5](https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html) - you need to agree to the license.
* [Ant 1.6.5](https://archive.apache.org/dist/ant/binaries/apache-ant-1.6.5-bin.zip)
* [JavaCC 4.0](https://github.com/javacc/javacc/tree/release_40)

For testing, you will need JUnit:

* [JUnit 4](https://github.com/junit-team/junit4/wiki/Download-and-Install) - to be placed in a special folder, details below.

Before we start, let's capture the path to the Jove git clone:

```bash
# From the jove clone:
export JOVE_CLONE=$(git rev-parse --show-toplevel)
```

## Installing Java 5

Download an install Java 5. Then set the `JAVA_HOME` environment variable to the installation directory:

```bash
export JAVA_HOME=<installation path>/jdk1.5.0_22
```

The file `$JAVA_HOME/bin/java` should exist. When you run it, it returns:

```
java version "1.5.0_22"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_22-b03)
Java HotSpot(TM) 64-Bit Server VM (build 1.5.0_22-b03, mixed mode)
```

## Installing Ant 1.6.5

Download and install Ant 1.6.5. When you run `ant -version` it will strangely return a different version number:

```bash
$ <path to ant>/bin/ant
Apache Ant(TM) version 1.9.7 compiled on April 9 2016
```

However if we ignore this and just continue, it will work.
To make things a bit easier, let's define an `ANT_HOME` environment variable:

```bash
export ANT_HOME=<path to ant installation>
```

The file `$ANT_HOME/bin/ant` should exist.

## Installing JavaCC 4.0

As of writing these lines no JavaCC binary package that works with Java 5 could not be found,
however it is still possible to compile JavaCC from source following these steps:

```bash

# If you have uploaded your SSH key to Github
git clone git@github.com:javacc/javacc.git

# Http access:
git clone https://github.com/javacc/javacc.git

# Go into the clone
cd javacc

# Checkout the release_40 tag and branch it
git checkout release_40 && git switch -c 4.0

# Build it
$ANT_HOME/bin/ant
```

You now need to set the `JAVACC_HOME` environment variable. Still from the root of the JavaCC Git clone, type:

```bash
export JAVACC_HOME=$(pwd)/bin/lib
```

The file `$JAVACC_HOME/javacc.jar` should exist.


## Building Jove (java part)

You are now ready to compile Jove (the java part).
Assuming you followed the steps above and have all three environment variables defined,
the following should compile Jove. From the Jove root git folder:

```bash
cd java
$ANT_HOME/bin/ant
```

When the build is successful, it concludes with:

```bash
BUILD SUCCESSFUL
Total time: 2 seconds
```

The API documentation in javadoc format can be found under `jove/docs/api/index.html`.

## Running the behavioral code sample

Jove has a behavioral code sample.
This behavioral sample does not need a verilog compiler.
You can run it with:

```bash
cd jove-sample
make behavioral
```

## Running the built-in tests

To run the built-in tests first obtain a JUnit jar file.
We are going to use JUnit 4.13.2.
The JUnit jar file must be renamed, as the Ant build system expects to find it as `$NEWISYS_JAVA/junit/junit.jar`.

Let's setup JUnit outside the jove clone:

```bash
cd <junit path>
curl -L -X GET "https://search.maven.org/remotecontent?filepath=junit/junit/4.13.2/junit-4.13.2.jar" -o junit.jar
mkdir junit && mv junit.jar junit
export NEWISYS_JAVA=$(pwd)
```

Going back to Jove, we run the `randsolver` component tests:

```bash
cd $JOVE_CLONE/java/randsolver
ant test
```

This step should conclude with:

```
test:
    [javac] Compiling 20 source files to /home/martin/git/martinda/jove/java/randsolver/bin

BUILD SUCCESSFUL
Total time: 1 second
```

Does this indicate that the test were executed?
