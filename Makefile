CONTIKI = ../..

all: unicast_receiver

CONTIKI_WITH_RIME = 1
include $(CONTIKI)/Makefile.include
