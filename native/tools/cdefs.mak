############################################################
# CompMake - Component-oriented Cross-platform Make Library
# Copyright (C) 2003 Trevor A. Robinson
# See the accompanying LICENSE file for license information.
############################################################

############################################################
# Generic settings
############################################################

C_SUFFIX := .c
CXX_SUFFIX := .cpp
H_SUFFIX := .h

SRC_DIRS := src/
INC_DIRS := include/

############################################################
# Target OS settings
############################################################

ifeq ($(TARGET_OS),win32)

  DEP_SUFFIX := .d
  DLL_SUFFIX := .dll
  EXE_SUFFIX := .exe
  IMP_SUFFIX := .lib
  LIB_SUFFIX := .lib
  OBJ_SUFFIX := .obj

endif

ifeq ($(TARGET_OS),linux)

  DEP_SUFFIX := .d
  DLL_PREFIX := lib
  DLL_SUFFIX := .so
  LIB_PREFIX := lib
  LIB_SUFFIX := .a
  OBJ_SUFFIX := .o

endif

ifeq ($(TARGET_OS),darwin)

  DEP_SUFFIX := .d
  DLL_PREFIX := lib
  DLL_SUFFIX := .dylib
  LIB_PREFIX := lib
  LIB_SUFFIX := .a
  OBJ_SUFFIX := .o

endif

