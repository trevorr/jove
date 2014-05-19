############################################################
# CompMake - Component-oriented Cross-platform Make Library
# Copyright (C) 2003 Trevor A. Robinson
# See the accompanying LICENSE file for license information.
############################################################

.PHONY: default all

default: all

ifneq ($(wildcard compdeps.mak),)
  include compdeps.mak
endif

%:
	make -C comp/$@ $(OP)

clean:
	rm -rf obj

