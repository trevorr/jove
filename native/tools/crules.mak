############################################################
# CompMake - Component-oriented Cross-platform Make Library
# Copyright (C) 2003 Trevor A. Robinson
# See the accompanying LICENSE file for license information.
############################################################

############################################################
# Compiler suite determination
# Required includes: host.mak
############################################################

ifndef COMPILER

  ifeq ($(HOST_OS),win32)

    ifeq ($(TARGET_OS),win32)
      ifdef CYGWIN
        COMPILER := gcc
      else
        COMPILER := msvc60
      endif
    endif

  endif

  ifeq ($(HOST_OS),linux)

    ifeq ($(TARGET_OS),linux)
      COMPILER := gcc
    endif

  endif

  ifeq ($(HOST_OS),darwin)

    ifeq ($(TARGET_OS),darwin)
      COMPILER := gcc
    endif

  endif

endif

ifndef COMPILER
  $(error Unsupported target OS for host OS)
endif

############################################################
# Compiler suite settings
# Required includes: cdefs.mak
# Required inputs:
#   OUT_NAME
#   OUT_TYPE
#   SRC_FILES
# Optional inputs:
#   BUILD_TYPE
#   OUT_DIR
#   TARGET_ARCH
#   TARGET_SUBSYS
#   RUNTIME_LINKING
#   RUNTIME_THREADING
#   INC_DIRS
#   LIB_DIRS
#   EXTRA_DEFINES
#   CC_FLAGS
#   CXX_FLAGS
#   CPP_FLAGS
#   LIB_FLAGS
#   LINK_FLAGS
#   STATIC_LIBS
#   SHARED_LIBS
#   EXT_STATIC_LIBS
#   EXT_SHARED_LIBS
#   IGNORE_LIBS
#   VERBOSE
#   ENABLE_RTTI
#   ENABLE_EH
#   WARN_LEVEL
#   WARN_AS_ERROR
#   OUT_VER_MAJ
#   OUT_VER_MIN
############################################################

#Default build type
ifndef BUILD_TYPE
  BUILD_TYPE := release
endif

#Build object file output directory
OBJ_DIR := $(OUT_DIR)/

#Create output directory immediately
$(shell $(MKDIR) $(OBJ_DIR))

ifndef SRC_FILES
  $(error No source files specified)
endif

#Build names of intermediate files
SRC_MODULES := $(basename $(SRC_FILES))
DEP_FILES := $(addprefix $(OBJ_DIR),$(addsuffix $(DEP_SUFFIX),$(SRC_MODULES)))
OBJ_FILES := $(addprefix $(OBJ_DIR),$(addsuffix $(OBJ_SUFFIX),$(SRC_MODULES)))

ifndef OUT_NAME
  $(error Output binary name not specified)
endif

#Get name of final output binary
OUT_LIB := $(OBJ_DIR)$(LIB_PREFIX)$(OUT_NAME)$(LIB_SUFFIX)
OUT_DLL := $(OBJ_DIR)$(DLL_PREFIX)$(OUT_NAME)$(DLL_SUFFIX)
OUT_EXE := $(OBJ_DIR)$(OUT_NAME)$(EXE_SUFFIX)

ifeq ($(OUT_TYPE),static)
  OUT_FILE := $(OUT_LIB)
endif
ifeq ($(OUT_TYPE),shared)
  OUT_FILE := $(OUT_DLL)
endif
ifeq ($(OUT_TYPE),exe)
  OUT_FILE := $(OUT_EXE)
endif

ifndef OUT_FILE
  $(error Valid output binary type not specified)
endif

#Platform-specific extra defines
ifeq ($(TARGET_OS),win32)
  EXTRA_DEFINES += WIN32 _WIN32
endif

ifeq ($(TARGET_OS),darwin)
  EXTRA_DEFINES += DARWIN _DARWIN
endif

#Build type extra defines
ifeq ($(BUILD_TYPE),debug)
  EXTRA_DEFINES += DEBUG _DEBUG
else
  EXTRA_DEFINES += NDEBUG
endif

#Microsoft Visual C++ 6.0
ifeq ($(COMPILER),msvc60)

  ifndef MSVCDIR
    $(error MSVCDir not defined)
  endif

  #Make does not like backslashes
  VCDIR := $(call makeify_path,$(MSVCDIR))

  #Search paths
  INC_DIRS += $(VCDIR)/INCLUDE
  LIB_DIRS += $(VCDIR)/LIB

  #Tool paths
  CC := $(VCDIR)/BIN/cl
  CXX := $(CC)
  DEP := cpp
  AR := $(VCDIR)/BIN/lib
  LD := $(VCDIR)/BIN/link

  #Tool flags and defines

  #Platform libraries
  EXT_SHARED_LIBS += kernel32 user32 gdi32 winspool comdlg32 advapi32 shell32 ole32 oleaut32 uuid odbc32 odbccp32 ws2_32

  #Target processor flags and defines
  ifneq ($(filter %86,$(TARGET_ARCH)),)
    EXTRA_DEFINES += _M_IX86 x86 i486 _x86_
    LIB_FLAGS += -machine:IX86
    LINK_FLAGS += -machine:IX86
  endif

  #Binary subtype flags and defines
  ifeq ($(TARGET_SUBSYS),console)
    EXTRA_DEFINES += _CONSOLE
    LIB_FLAGS += -subsystem:console
    LINK_FLAGS += -subsystem:console
  else
    EXTRA_DEFINES += _WINDOWS
    LIB_FLAGS += -subsystem:windows
    LINK_FLAGS += -subsystem:windows
  endif

  #Runtime library type
  ifneq ($(filter static,$(RUNTIME_LINKING)),)
    ifneq ($(filter single,$(RUNTIME_THREADING)),)
      #static single-threaded
      RUNTIME_FLAG := -ML
    else
      #static multi-threaded
      RUNTIME_FLAG := -MT
      DEP_FLAGS += -D_MT
    endif
  else
    #shared (multi-threaded)
    RUNTIME_FLAG := -MD
    DEP_FLAGS += -D_DLL -D_MT
  endif

  #C/C++ preprocessor flags
  CPP_FLAGS += $(addprefix -I,$(INC_DIRS))
  CPP_FLAGS += $(addprefix -D,$(EXTRA_DEFINES))

  #C/C++ dependency generator flags
  #Suppress warnings when generating dependencies
  DEP_FLAGS += -w
  #Make cpp look like MS Visual C++
  DEP_FLAGS += -U__GNUC__ -D_MSC_VER=1200

  #C/C++ compiler flags
  ifeq ($(call enabled,$(VERBOSE),cc),)
    CC_FLAGS += -nologo
  endif
  ifneq ($(call enabled,$(ENABLE_RTTI)),)
    CXX_FLAGS += -GR
  endif
  ifneq ($(call enabled,$(ENABLE_EH)),)
    CXX_FLAGS += -GX
  endif
  ifdef WARN_LEVEL
    CC_FLAGS += -W$(WARN_LEVEL)
  endif
  ifneq ($(call enabled,$(WARN_AS_ERROR)),)
    CC_FLAGS += -WX
  endif

  #Librarian flags
  ifeq ($(call enabled,$(VERBOSE),lib),)
    LIB_FLAGS += -nologo
  endif

  #Linker flags
  ifeq ($(call enabled,$(VERBOSE),link),)
    LINK_FLAGS += -nologo
  else
    LINK_FLAGS += -verbose
  endif
  LINK_FLAGS += $(addprefix -libpath:,$(LIB_DIRS))
  LINK_FLAGS += $(addprefix -nodefaultlib:,$(IGNORE_LIBS))
  LINK_FLAGS += -fixed:no
  LINK_FLAGS += -incremental:no
  ifdef OUT_VER_MAJ
    LINK_FLAGS += -version:$(OUT_VER_MAJ).$(OUT_VER_MIN)
  endif

  #Shared library linker flags
  DLL_FLAGS += -dll

  #Debug/release compiler and linker flags
  ifeq ($(BUILD_TYPE),debug)
    ifdef RUNTIME_FLAG
      RUNTIME_FLAG := $(RUNTIME_FLAG)d
    endif
    CC_FLAGS += $(RUNTIME_FLAG) -Od -GZ -Zi
    LINK_FLAGS += -debug
  else
    CC_FLAGS += $(RUNTIME_FLAG)
    ifeq ($(OPTIMIZE),speed)
      CC_FLAGS += -O2
    endif
    ifeq ($(OPTIMIZE),size)
      CC_FLAGS += -O1
    endif
    ifeq ($(OPTIMIZE),off)
      CC_FLAGS += -Od
    endif
    LINK_FLAGS += -release
  endif

  #Dependency rules

define DEP_CMD
	$(MKDIR) $(@D)
	$(DEP) -MM $(CPP_FLAGS) $(DEP_FLAGS) $< | sed -e "s/.*$(call regex_slash,$*).o *:/\$$(OBJ_DIR)$(call regex_slash,$*)$(OBJ_SUFFIX) \$$(OBJ_DIR)$(call regex_slash,$*)$(DEP_SUFFIX):/g" > $@
endef

$(OBJ_DIR)%$(DEP_SUFFIX): %$(C_SUFFIX)
	$(DEP_CMD)

$(OBJ_DIR)%$(DEP_SUFFIX): %$(CXX_SUFFIX)
	$(DEP_CMD)

  #Compile rules

$(OBJ_DIR)%$(OBJ_SUFFIX): %$(C_SUFFIX)
	$(RM) $@
	$(CC) $(CC_FLAGS) $(CPP_FLAGS) -Fo$@ -c -Tc$<

$(OBJ_DIR)%$(OBJ_SUFFIX): %$(CXX_SUFFIX)
	$(RM) $@
	$(CXX) $(CC_FLAGS) $(CXX_FLAGS) $(CPP_FLAGS) -Fo$@ -c -Tp$<

  #Link rules

  LINK_DEPS := $(OBJ_FILES)
  #Use special GNU 'make' feature for expanding library names in
  #prerequisites prefixed with -l.
  LINK_DEPS += $(addprefix -l,$(STATIC_LIBS) $(SHARED_LIBS))

  #Simply specify filenames of external libraries.  Place external object
  #files first, since they may override the contents of the static libraries.
  EXT_LINK_LIBS := $(EXT_OBJ_FILES)
  EXT_LINK_LIBS += $(addsuffix $(LIB_SUFFIX),$(EXT_STATIC_LIBS))
  EXT_LINK_LIBS += $(addsuffix $(IMP_SUFFIX),$(EXT_SHARED_LIBS))

$(OUT_LIB): $(OBJ_FILES)
	$(RM) $@
	$(AR) $(LIB_FLAGS) -out:$@ $^

$(OUT_DLL): $(LINK_DEPS)
	$(RM) $@
	$(LD) $(LINK_FLAGS) $(DLL_FLAGS) -out:$@ $^ $(EXT_LINK_LIBS)

$(OUT_EXE): $(LINK_DEPS)
	$(RM) $@
	$(LD) $(LINK_FLAGS) -out:$@ $^ $(EXT_LINK_LIBS)

endif

#GNU Compiler Collection
ifeq ($(COMPILER),gcc)

  #Tool paths
  CC := gcc
  CXX := g++
  DEP := gcc -E
  AR := ar rs

  #Determine method to link with C++ library
  ifneq ($(filter cxx-shared,$(RUNTIME_LINKING)),)
    GCC_CXX_MODE := shared
  endif

  ifneq ($(filter cxx-static,$(RUNTIME_LINKING)),)
    GCC_CXX_MODE := static
  endif

  ifneq ($(filter cxx-none,$(RUNTIME_LINKING)),)
    GCC_CXX_MODE := none
  endif

  #If C++ runtime type is not specified,
  #set it based on whether SRC_FILES contains C++ source files.
  ifndef GCC_CXX_MODE
    ifneq ($(filter %$(CXX_SUFFIX),$(SRC_FILES)),)
      #C++ files are present; link static C++ library
      GCC_CXX_MODE := static
    else
      #C++ files are not present; no need to link C++ library
      GCC_CXX_MODE := none
    endif
  endif

  ifeq ($(GCC_CXX_MODE),shared)
    #Only cxx-shared uses g++
    LD := g++
  else
    #cxx-static and cxx-none use gcc
    LD := gcc
  endif

  ifeq ($(GCC_CXX_MODE),static)
    #Determine path to libstdc++.a
    LIBSTDCXX_A := $(shell gcc -print-search-dirs |grep libraries: |cut -f2 -d" ")
    LIBSTDCXX_A := $(subst :, ,$(LIBSTDCXX_A))
    LIBSTDCXX_A := $(call expand_path,libstdc++.a,$(LIBSTDCXX_A))

    #Add libstdc++.a to link libs
    LINK_LIBS += $(LIBSTDCXX_A)
  endif

  #Tool flags and defines

  #Linker supports importing symbols directly from a DLL
  DIRECT_DLL_IMPORT := 1

  #Platform libraries
  EXT_SHARED_LIBS += pthread
  ifneq ($(HOST_OS),win32)
    EXT_SHARED_LIBS += dl
  endif

  #Target processor flags and defines
  ifneq ($(filter %86,$(TARGET_ARCH)),)
    EXTRA_DEFINES += i386 $(TARGET_ARCH)
  endif

  #C/C++ preprocessor flags
  CPP_FLAGS += $(addprefix -I,$(INC_DIRS))
  CPP_FLAGS += $(addprefix -D,$(EXTRA_DEFINES))

  #C/C++ dependency generator flags
  #Suppress warnings when generating dependencies
  DEP_FLAGS += -w

  #C/C++ compiler flags
  ifneq ($(HOST_OS),win32)
    CC_FLAGS += -fPIC
  endif
  ifneq ($(call enabled,$(VERBOSE),cc),)
    CC_FLAGS += -v
  endif
  ifeq ($(call enabled,$(ENABLE_RTTI)),)
    CXX_FLAGS += -fno-rtti
  endif
  ifeq ($(call enabled,$(ENABLE_EH)),)
    CXX_FLAGS += -fno-exceptions
  endif
  ifeq ($(WARN_LEVEL),0)
    CC_FLAGS += -w
  endif
  ifneq ($(filter 1 2 3,$(WARN_LEVEL)),)
    CC_FLAGS += -Wall
  endif
  ifeq ($(WARN_LEVEL),4)
    CC_FLAGS += -W -Wall
  endif
  ifneq ($(call enabled,$(WARN_AS_ERROR)),)
    CC_FLAGS += -Werror
  endif
  ifeq ($(HOST_OS),darwin)
    ifeq ($(OUT_TYPE),shared)
      CC_FLAGS += -dynamic
    endif
  endif

  #Linker flags
  ifneq ($(call enabled,$(VERBOSE),link),)
    LINK_FLAGS += -v
  endif
  LINK_FLAGS += $(addprefix -L,$(LIB_DIRS))

  #Shared library linker flags
  ifeq ($(HOST_OS),darwin)
    DLL_FLAGS += -flat_namespace -bundle -undefined suppress
  else
    DLL_FLAGS += -shared
    ifeq ($(HOST_OS),win32)
        DLL_FLAGS += -Wl,--export-all -Wl,--enable-auto-image-base -Wl,--export-dynamic
    endif
  endif

  #Debug/release compiler and linker flags
  ifeq ($(BUILD_TYPE),debug)
    CC_FLAGS += -g
  else
    ifeq ($(OPTIMIZE),speed)
      CC_FLAGS += -O3
    endif
    ifeq ($(OPTIMIZE),size)
      CC_FLAGS += -Os
    endif
    ifeq ($(OPTIMIZE),off)
      CC_FLAGS += -O0
    endif
  endif

  #Dependency rules

define DEP_CMD
	$(MKDIR) $(@D)
	$(DEP) -MM $(CPP_FLAGS) $(DEP_FLAGS) $< | sed -e "s/.*$(call regex_slash,$*).o *:/\$$(OBJ_DIR)$(call regex_slash,$*)$(OBJ_SUFFIX) \$$(OBJ_DIR)$(call regex_slash,$*)$(DEP_SUFFIX):/g" > $@
endef

$(OBJ_DIR)%$(DEP_SUFFIX): %$(C_SUFFIX)
	$(DEP_CMD)

$(OBJ_DIR)%$(DEP_SUFFIX): %$(CXX_SUFFIX)
	$(DEP_CMD)

  #Compile rules

$(OBJ_DIR)%$(OBJ_SUFFIX): %$(C_SUFFIX)
	$(RM) $@
	$(CC) $(CC_FLAGS) $(CPP_FLAGS) -o $@ -c $<

$(OBJ_DIR)%$(OBJ_SUFFIX): %$(CXX_SUFFIX)
	$(RM) $@
	$(CXX) $(CC_FLAGS) $(CXX_FLAGS) $(CPP_FLAGS) -o $@ -c $<

  #Link rules

$(OUT_LIB): $(OBJ_FILES)
	$(RM) $@
	$(AR) $@ $(OBJ_FILES)

  #Use special GNU 'make' feature for expanding library names in
  #prerequisites prefixed with -l.
  LINK_DEPS := $(OBJ_FILES) $(addprefix -l,$(STATIC_LIBS) $(SHARED_LIBS))

  #Do not use expanded library prerequisite paths in linker command,
  #since they would be stored in the final executable.  Instead, we
  #must use the -l<libname> and -L<libpath> linker flags.
  LINK_LIBS += $(addprefix -l,$(STATIC_LIBS))
  LINK_LIBS += $(addprefix -l,$(EXT_STATIC_LIBS))
  LINK_LIBS += $(addprefix -l,$(SHARED_LIBS) $(EXT_SHARED_LIBS))

$(OUT_DLL): $(LINK_DEPS)
	$(RM) $@
	$(LD) $(LINK_FLAGS) $(DLL_FLAGS) -o $@ $(OBJ_FILES) $(LINK_LIBS)

$(OUT_EXE): $(LINK_DEPS)
	$(RM) $@
	$(LD) $(LINK_FLAGS) -o $@ $(OBJ_FILES) $(LINK_LIBS)

endif

############################################################
# Standard rules
############################################################

.PHONY: deps objs

build: deps objs $(OUT_FILE)

deps: $(DEP_FILES)

objs: $(OBJ_FILES)

clean:
	rm -rf $(OUT_DIR)

############################################################
# Set virtual search paths
############################################################

#Set source search paths

SRC_VPATHS := $(SRC_DIRS) $(OBJ_DIR)

#Add standard source suffixes
vpath %$(C_SUFFIX) $(SRC_VPATHS)
vpath %$(CXX_SUFFIX) $(SRC_VPATHS)
vpath %$(H_SUFFIX) $(SRC_VPATHS)

#Set library search paths

#Clear any default library search patterns
.LIBPATTERNS :=

#Add shared object (DLL) library search pattern if supported
ifdef DIRECT_DLL_IMPORT
  .LIBPATTERNS += $(DLL_PREFIX)%$(DLL_SUFFIX)
  vpath %$(DLL_SUFFIX) $(LIB_DIRS) $(OBJ_DIR)
endif

#Add import library search pattern if defined
ifdef IMP_SUFFIX
  .LIBPATTERNS += %$(IMP_SUFFIX)
  vpath %$(IMP_SUFFIX) $(LIB_DIRS)
endif

#Add static library search pattern if different from import library pattern
ifneq ($(IMP_SUFFIX),$(LIB_SUFFIX))
  .LIBPATTERNS += $(LIB_PREFIX)%$(LIB_SUFFIX)
  vpath %$(LIB_SUFFIX) $(LIB_DIRS)
endif

#Windows supports searching for object files in the library path
ifeq ($(TARGETOS),win32)
  vpath %$(OBJ_SUFFIX) $(LIB_DIRS)
endif

############################################################
# Include source file dependencies
############################################################

ifeq ($(filter clean,$(MAKECMDGOALS)),)

  EXIST_DEP_FILES := $(foreach dep,$(DEP_FILES),$(wildcard $(dep)))

  ifdef EXIST_DEP_FILES
    include $(EXIST_DEP_FILES)
  endif

endif
