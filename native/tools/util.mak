############################################################
# CompMake - Component-oriented Cross-platform Make Library
# Copyright (C) 2003 Trevor A. Robinson
# See the accompanying LICENSE file for license information.
############################################################

############################################################
# Utility definitions
############################################################

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
COMMA := ,

expand_path = $(firstword $(foreach path,$(2),$(wildcard $(path)/$1)))
expand_paths = $(foreach file,$(1),$(call expand_path,$(file),$(2)))

enabled = $(filter on yes true $(2),$(1))
disabled = $(filter off no false $(2),$(1))

