############################################################
# CompMake - Component-oriented Cross-platform Make Library
# Copyright (C) 2003 Trevor A. Robinson
# See the accompanying LICENSE file for license information.
############################################################

############################################################
# Host OS determination
############################################################

#HACK: HOST_OS seems to be improperly set sometimes
HOST_OS :=

#Environment-based host OS determination
ifndef HOST_OS

  ifeq ($(OS),Windows_NT)
    HOST_OS := win32

    ifeq ($(findstring CYGWIN,$(shell uname -s)),CYGWIN)
      CYGWIN := cygwin
    endif
  endif

endif

#uname-based host OS determination
ifndef HOST_OS

  UNAME_OS := $(shell uname -s)

  ifeq ($(findstring CYGWIN,$(UNAME_OS)),CYGWIN)
    HOST_OS := win32
    CYGWIN := cygwin
  endif

  ifeq ($(findstring indows,$(UNAME_OS)),indows)
    HOST_OS := win32
  endif

  ifeq ($(findstring inux,$(UNAME_OS)),inux)
    HOST_OS := linux
  endif

  ifeq ($(findstring arwin,$(UNAME_OS)),arwin)
    HOST_OS := darwin
  endif

endif

ifndef HOST_OS
  $(error Unknown host OS)
endif

#TARGET_OS defaults to HOST_OS
ifndef TARGET_OS
  TARGET_OS := $(HOST_OS)
endif

############################################################
# Host architecture determination
############################################################

ifndef HOST_ARCH

  ifeq ($(HOST_OS),win32)

    ifdef $(PROCESSOR_ARCHITECTURE)
      #Use WinNT environment variable
      HOST_ARCH := $(PROCESSOR_ARCHITECTURE)
    else
      #Win9X is always x86
      HOST_ARCH := x86
    endif

  else

    ifeq ($(HOST_OS),darwin)
        # Get processor type (e.g. powerpc)
        HOST_ARCH := $(shell uname -p)
    else
        #Use uname machine type for Unix (e.g. i586)
        HOST_ARCH := $(shell uname -m)
    endif

  endif

endif

ifndef HOST_ARCH
  $(error Unknown host architecture)
endif

#TARGET_OS defaults to HOST_OS
ifndef TARGET_ARCH
  TARGET_ARCH := $(HOST_ARCH)
endif

############################################################
# Host OS settings
# Defines:
#   SLASH - the platform name separator (/ on Unix, \ on Windows)
#   REGEX_SLASH - name separator to use in regular expressions
#   SEP - the platform path separator (: on Unix, ; on Windows)
#   fix_slash - replaces SLASH with / in the given string
#   makeify_path - translates the given native path to a form that
#     make can handle (i.e. / name separator, no spaces); the given
#     path should exist
#   regex_slash - replaces / with REGEX_SLASH in the given string
#   MKDIR - directory creation command that handles creating intermediate
#     directories and does nothing if the directory already exists
#   RM - file/directory removal command that does nothing if the file or
#     directory does not exist
############################################################

ifeq ($(HOST_OS),win32)

  SLASH := \$(EMPTY)
  REGEX_SLASH := $(SLASH)
  SEP := ;

  fix_slash = $(subst $(SLASH),/,$(1))
  ifdef CYGWIN
    makeify_path = $(shell cygpath -d -m "$(1)")
  else
    makeify_path = $(fix_slash)
  endif

else

  SLASH := /
  REGEX_SLASH := \\/
  SEP := :

  fix_slash = $(1)
  makeify_path = $(1)

endif

regex_slash = $(subst /,$(REGEX_SLASH),$(1))

MKDIR := mkdir -p
RM := rm -rf

