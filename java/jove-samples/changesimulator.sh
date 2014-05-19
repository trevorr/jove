#!/bin/sh

SIM=$1
PLI4J_HOME=$2

if [ "$SIM" == "" -o "$PLI4J_HOME" == "" ]
then
    echo "usage: $0 <sim> <pli4j_home>"
    exit 1
fi

PLI4J_CONF=${PLI4J_HOME}/comp/pli4j/src/Configuration.h
IS_NOT_VCS=`grep "//#define SIMULATOR_VCS" ${PLI4J_CONF}`

if [ "$SIM" == "vcs" ]
then
    if [ "$IS_NOT_VCS" != "" ]
    then
        echo "Not configured for VCS. Configuring for VCS."
        sed -i -e "s/\/\/#define SIMULATOR_VCS/#define SIMULATOR_VCS/" ${PLI4J_CONF}
    else
        echo "Configured for VCS. No changes are required"
    fi
else
    # check if vcs is current simulator. if so, comment that out
    if [ "$IS_NOT_VCS" == "" ]
    then
        echo "Configured for VCS. Deonfiguring for VCS."
        sed -i -e "s/#define SIMULATOR_VCS/\/\/#define SIMULATOR_VCS/" ${PLI4J_CONF}
    else
        echo "Not configured for VCS. No changes are required"
    fi
fi 
    

