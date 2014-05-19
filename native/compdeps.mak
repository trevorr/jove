jvmlibpaths jvmtest : jvminvoke
pli4j : jnicpp vpicpp
hellojava : pli4j jvmlibpaths

# include any site-specific compdeps if they exist
ifneq ($(wildcard extracompdeps.mak),)
  include extracompdeps.mak
endif

