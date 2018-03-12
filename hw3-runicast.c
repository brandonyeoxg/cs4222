/*
 * Copyright (c) 2007, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

/**
 * \file
 *         Reliable single-hop unicast example
 * \author
 *         Adam Dunkels <adam@sics.se>
 */

#include <stdio.h>

#include "contiki.h"
#include "net/rime/rime.h"

#include "lib/list.h"
#include "lib/memb.h"

#include "dev/button-sensor.h"
#include "dev/leds.h"

#define MAX_RETRANSMISSIONS 4
#define NUM_HISTORY_ENTRIES 4
#define PAYLOAD_SIZE        12
#define EXT_FLASH_BASE_ADDR 0
#define EXT_FLASH_SIZE      32*1024
#define TRANSMISSION_DELAY  10 * CLOCK_SECOND

/* RCV addr */
#define RCV_ADDR_0          179
#define RCV_ADDR_1          130

#define DEBUG

/*---------------------------------------------------------------------------*/
PROCESS(runicast_process, "runicast test");
AUTOSTART_PROCESSES(&runicast_process);
/*---------------------------------------------------------------------------*/
/* OPTIONAL: Sender history.
 * Detects duplicate callbacks at receiving nodes.
 * Duplicates appear when ack messages are lost. */
struct history_entry {
  struct history_entry *next;
  linkaddr_t addr;
  uint8_t seq;
};
LIST(history_table);
MEMB(history_mem, struct history_entry, NUM_HISTORY_ENTRIES);
/*---------------------------------------------------------------------------*/
static void
recv_runicast(struct runicast_conn *c, const linkaddr_t *from, uint8_t seqno)
{
  /* OPTIONAL: Sender history */
  struct history_entry *e = NULL;
  for(e = list_head(history_table); e != NULL; e = e->next) {
    if(linkaddr_cmp(&e->addr, from)) {
      break;
    }
  }
  if(e == NULL) {
    /* Create new history entry */
    e = memb_alloc(&history_mem);
    if(e == NULL) {
      e = list_chop(history_table); /* Remove oldest at full history */
    }
    linkaddr_copy(&e->addr, from);
    e->seq = seqno;
    list_push(history_table, e);
  } else {
    /* Detect duplicate callback */
    if(e->seq == seqno) {
      printf("runicast message received from %d.%d, seqno %d (DUPLICATE)\n",
        from->u8[0], from->u8[1], seqno);
      return;
    }
    /* Update existing history entry */
    e->seq = seqno;
  }

  printf("runicast message received from %d.%d, seqno %d ",
    from->u8[0], from->u8[1], seqno);

  // prints all the data
  int payloadSize = packetbuf_datalen();
  int i;
  int *payload = (int*)packetbuf_dataptr();
  printf(" payload size: %d:", payloadSize);
  for (i = 0; i < payloadSize; ++i) {
    printf(" %d", *(payload + i));
  }
  printf("\n");
}

static void
sent_runicast(struct runicast_conn *c, const linkaddr_t *to, uint8_t retransmissions)
{
  printf("runicast message sent to %d.%d, retransmissions %d\n",
    to->u8[0], to->u8[1], retransmissions);
}
static void
timedout_runicast(struct runicast_conn *c, const linkaddr_t *to, uint8_t retransmissions)
{
  printf("runicast message timed out when sending to %d.%d, retransmissions %d\n",
    to->u8[0], to->u8[1], retransmissions);
}
static const struct runicast_callbacks runicast_callbacks = {recv_runicast,
  sent_runicast,
  timedout_runicast};
  static struct runicast_conn runicast;
/*---------------------------------------------------------------------------*/

  static int obtainPayload(int *address_offset, int *payload) {
    printf("READING FROM FLASH:\n");
    int executed = ext_flash_open();
    if(!executed) {
      printf("Cannot open flash\n");
      ext_flash_close();
      return 0;
    }
    int payloadIdx;
    static int sensor_data_int[1];
    for(payloadIdx = 0; payloadIdx < PAYLOAD_SIZE; ++payloadIdx) {
      if(EXT_FLASH_BASE_ADDR + (*address_offset) >= EXT_FLASH_SIZE) {
        ext_flash_close();
        return payloadIdx + 1;
      }
      ext_flash_read((*address_offset), sizeof(sensor_data_int),  (int *)&sensor_data_int);
      *(payload + payloadIdx) = sensor_data_int[0];
      (*address_offset) += sizeof(sensor_data_int);
    }
    ext_flash_close();
    return PAYLOAD_SIZE;
  }

  static void sendPayload(struct runicast_conn *runicast, int payloadSize, int *payload) {
      linkaddr_t recv;
      packetbuf_copyfrom(payload, payloadSize * sizeof(int));
      recv.u8[0] = RCV_ADDR_0;
      recv.u8[1] = RCV_ADDR_1;
      printf("%u.%u: sending runicast to address %u.%u\n",
        linkaddr_node_addr.u8[0],
        linkaddr_node_addr.u8[1],
        recv.u8[0],
        recv.u8[1]);
      runicast_send(runicast, &recv, MAX_RETRANSMISSIONS);
  }

/*---------------------------------------------------------------------------*/
  PROCESS_THREAD(runicast_process, ev, data)
  {
    PROCESS_EXITHANDLER(runicast_close(&runicast);)

    PROCESS_BEGIN();
    static struct etimer et;
    runicast_open(&runicast, 144, &runicast_callbacks);
  /* OPTIONAL: Sender history */
    list_init(history_table);
    memb_init(&history_mem);  

  /* Initalise code for data reading from flash */
    static int address_offset = 0;
    int pointer = EXT_FLASH_BASE_ADDR + address_offset;

    /* Receiver node: do nothing */
    if(linkaddr_node_addr.u8[0] == RCV_ADDR_0 &&
       linkaddr_node_addr.u8[1] == RCV_ADDR_1) {
      PROCESS_WAIT_EVENT_UNTIL(0);
    }  
    static int seqNum = 0;
    while(pointer < EXT_FLASH_SIZE) {
      etimer_set(&et, TRANSMISSION_DELAY);
      PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
      
      if(!runicast_is_transmitting(&runicast)) {
        int payload[PAYLOAD_SIZE] = { 0 };
        int payloadSize = 0;        
        payloadSize = obtainPayload(&address_offset, payload);

#ifdef DEBUG
        int k;
        printf("Payload seqno %d payload size: %d:", seqNum, payloadSize);
        for(k = 0; k < payloadSize; k++) {
          printf(" %d", payload[k]);
        }
        printf("\n");
#endif
        sendPayload(&runicast, payloadSize, payload);
        pointer = EXT_FLASH_BASE_ADDR + address_offset;
        seqNum += 1;
      }
    }
    PROCESS_END();
  }
/*---------------------------------------------------------------------------*/
