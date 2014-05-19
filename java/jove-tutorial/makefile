ifndef PLI4J_HOME
    ifneq ($(wildcard ../native),)
        # binary distribution
        PLI4J_HOME := ../native
    else
        ifneq ($(wildcard ../../native),)
            # source distribution
            PLI4J_HOME := ../../native
        endif
    endif
endif
ifeq ($(wildcard $(PLI4J_HOME)/comp),)
    $(error Please set PLI4J_HOME to point to the root of the PLI4J directory)
endif

include $(PLI4J_HOME)/tools/util.mak

# include host.mak to get SEP variable and escape it if it's a semicolon
include $(PLI4J_HOME)/tools/host.mak

# include cdefs.mak to get DLL_(PREFIX|SUFFIX)
include $(PLI4J_HOME)/tools/cdefs.mak

# Windows uses semicolon delimited classpaths, but in cygwin, the semicolons
# need to be escaped
ifeq ($(SEP),;)
    SEP := \;
endif

# makefile for building the jove-samples
.PHONY: java clean clean-verilog change_simulator require_simulator

CLASSPATH_PROJECTS := jove langschema langschema-java langschema-jove newisys-utils randsolver
ifneq ($(wildcard ../bin/jove.jar),)
    # binary distribution
    CLASSPATH_PREFIX := ../bin/
    CLASSPATH_SUFFIX := .jar
else
    # source distribution
    CLASSPATH_PREFIX := ../
    CLASSPATH_SUFFIX := /bin
endif
CLASSPATH := $(subst $(SPACE),$(SEP),$(foreach proj,$(CLASSPATH_PROJECTS),$(CLASSPATH_PREFIX)$(proj)$(CLASSPATH_SUFFIX)) bin)

SIMULATOR ?= vcs
SIMULATOR_FLAGS := +javaclasspath=$(CLASSPATH)

memctrl: java change_simulator
	make -C $(PLI4J_HOME) SIMULATOR=$(SIMULATOR) pli4j
	$(SIMULATOR_PATH)/$(SIMULATOR) $(SIMULATOR_FLAGS) +javaclass=com.newisys.tutorial.memctrl.MemctrlTestcase src/com/newisys/tutorial/memctrl/memory.v src/com/newisys/tutorial/memctrl/memctrl_test_top.v gensrc/com/newisys/tutorial/memctrl/memctrl_shell.v 

require_simulator:
    # require a simulator if the target includes an RTL example
    ifeq ($(SIMULATOR),vcs)
        ifneq ($(VCS_HOME),)
            ifeq ($(wildcard $(VCS_HOME)/*),)
                $(error Please set VCS_HOME to point to the root of the VCS directory)
            endif
        else
            $(error Please set VCS_HOME to point to the root of the VCS directory)
        endif

        PLI4J_TAB := $(PLI4J_HOME)/comp/pli4j/src/pli4j.tab
        PLI4J_STATIC_LIBS := pli4j jnicpp vpicpp
        PLI4J_STATIC_LIB_PATHS := $(foreach lib,$(PLI4J_STATIC_LIBS),$(PLI4J_HOME)/obj/$(lib)/lib$(lib).a)
        SIMULATOR_PATH := $(VCS_HOME)/bin
        SIMULATOR_FLAGS += +v2k +vpi -R -P $(PLI4J_TAB) $(PLI4J_STATIC_LIB_PATHS) -cc g++ -LDFLAGS -g -lpthread
    else
        ifeq ($(SIMULATOR),cver)
            ifneq ($(CVER_HOME),)
                ifeq ($(wildcard $(CVER_HOME)/*),)
                    $(error Please set CVER_HOME to point to the root of the cver directory)
                endif
            else
                $(error Please set CVER_HOME to point to the root of the cver directory)
            endif

            PLI4J_LIB := $(PLI4J_HOME)/obj/pli4j/$(DLL_PREFIX)pli4j$(DLL_SUFFIX)
            SIMULATOR_PATH := $(CVER_HOME)/bin
            SIMULATOR_FLAGS += +change_port_type +loadvpi=$(PLI4J_LIB):pli4j_register_vpi_tasks
        else
            $(error Unsupported simulator: $(SIMULATOR))
        endif
    endif

change_simulator: require_simulator
	/bin/sh changesimulator.sh $(SIMULATOR) $(PLI4J_HOME)

java: 
	ant

clean-verilog:
	rm -rf simv* csrc

clean: clean-verilog
	ant clean
	make -C $(PLI4J_HOME) clean
