#include "contiki.h"
#include <stdio.h> 
/*---------------------------------------------------------------------------*/
#define EXT_FLASH_BASE_ADDR     0
#define EXT_FLASH_SIZE          4*1024*1024
#define TIME_SLICE              0
#define X_AXIS                  1
#define Y_AXIS                  2
#define Z_AXIS                  3
/*---------------------------------------------------------------------------*/
PROCESS(read_external_flash, "Read external flash");
AUTOSTART_PROCESSES(&read_external_flash);
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(read_external_flash, ev, data){
  PROCESS_BEGIN();

  static int address_offset = 0;
  static int sensor_data_int[4];

  printf("READING FROM FLASH:\n");

  int executed = ext_flash_open();

  if(!executed) {
    printf("Cannot open flash\n");
    ext_flash_close();
    return 0;
  }

  int pointer = EXT_FLASH_BASE_ADDR + address_offset;
  while(pointer < EXT_FLASH_SIZE){
    executed = ext_flash_read(address_offset, sizeof(sensor_data_int),  (int *)&sensor_data_int);
    printf("Time slice:%d, Acc X: %d, Acc Y: %d, Acc Z: %d\n", sensor_data_int[TIME_SLICE] ,sensor_data_int[X_AXIS], sensor_data_int[Y_AXIS], sensor_data_int[Z_AXIS]);
    address_offset += sizeof(sensor_data_int);
    pointer = EXT_FLASH_BASE_ADDR + address_offset;
  
  }
  ext_flash_close();
  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
