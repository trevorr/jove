############################################################
# Initial includes
############################################################

ROOT_DIR := ../..
ROOT_COMP_DIR := $(ROOT_DIR)/comp
ROOT_TOOLS_DIR := $(ROOT_DIR)/tools
ROOT_OBJ_DIR := $(ROOT_DIR)/obj

include $(ROOT_TOOLS_DIR)/util.mak
include $(ROOT_TOOLS_DIR)/host.mak
include $(ROOT_TOOLS_DIR)/cdefs.mak

############################################################
# Check for a valid simulator type
############################################################
ifeq ($(SIMULATOR),)
    $(warning SIMULATOR not specified -- defaulting to VCS)
    SIMULATOR := vcs
endif

# read the OUT_TYPE from the simulator specific makefile
include makefile.$(SIMULATOR)


############################################################
# Project settings
############################################################

.PHONY: default build

default: build

COMP_NAME := pli4j
OUT_NAME := $(notdir $(COMP_NAME))
OUT_DIR := $(ROOT_OBJ_DIR)/$(COMP_NAME)
SRC_FILES := \
	Utilities.cpp \
	JavaObjectCache.cpp \
	ConversionFunctions.cpp \
	FinishSupport.cpp \
	CallbackHandler.cpp \
	com_newisys_verilog_pli_PLI.cpp \
	com_newisys_ova_OVA.cpp \
	pli4j.cpp

JAVA_INC := $(call makeify_path,$(JAVA_HOME))/include
INC_DIRS += $(JAVA_INC) $(JAVA_INC)/$(TARGET_OS)

INC_DIRS += $(ROOT_COMP_DIR)/jnicpp/include

INC_DIRS += $(ROOT_COMP_DIR)/vpicpp/include

INC_DIRS += $(VCS_HOME)/include

ENABLE_RTTI := true
ENABLE_EH := true

############################################################
# Final includes
############################################################

include $(ROOT_TOOLS_DIR)/crules.mak

