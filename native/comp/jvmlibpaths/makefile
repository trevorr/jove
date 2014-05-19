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
# Project settings
############################################################

.PHONY: default build

default: build

COMP_NAME := jvmlibpaths
OUT_NAME := $(notdir $(COMP_NAME))
OUT_TYPE := exe
TARGET_SUBSYS := console
OUT_DIR := $(ROOT_OBJ_DIR)/$(COMP_NAME)
SRC_FILES := jvmlibpaths.c

JAVA_INC := $(call makeify_path,$(JAVA_HOME))/include
INC_DIRS += $(JAVA_INC) $(JAVA_INC)/$(TARGET_OS)

INC_DIRS += $(ROOT_COMP_DIR)/jvminvoke/include
LIB_DIRS += $(ROOT_OBJ_DIR)/jvminvoke
STATIC_LIBS += jvminvoke

############################################################
# Final includes
############################################################

include $(ROOT_TOOLS_DIR)/crules.mak

