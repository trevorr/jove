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

COMP_NAME := vpicpp
OUT_NAME := $(notdir $(COMP_NAME))
OUT_TYPE := static
OUT_DIR := $(ROOT_OBJ_DIR)/$(COMP_NAME)
SRC_FILES := vpicpp.cpp

ENABLE_EH := true
ENABLE_RTTI := true

############################################################
# Final includes
############################################################

include $(ROOT_TOOLS_DIR)/crules.mak

