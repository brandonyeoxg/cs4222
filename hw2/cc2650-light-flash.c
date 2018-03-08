/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "dev/leds.h"
#include "button-sensor.h"
#include "board-peripherals.h"
#include "node-id.h"
#include <stdio.h>
/*---------------------------------------------------------------------------*/
#define CC26XX_DEMO_LOOP_INTERVAL           (CLOCK_SECOND * 0.5)
#define CC26XX_BUTTON_LEFT                  &button_left_sensor
#define CC26XX_BUTTON_RIGHT                 &button_right_sensor
#define EXT_FLASH_BASE_ADDR_SENSOR_DATA     0 
#define EXT_FLASH_MEMORY_END_ADDRESS        0x400010 
#define EXT_FLASH_BASE_ADDR                 0
#define EXT_FLASH_SIZE                      4*1024*1024
#define version					                    "1.0"
/*---------------------------------------------------------------------------*/
static struct etimer et, et_blink;
static bool write_to_ext_flash = false;
static uint8_t blinks;
static int light=-1;
static int address_index_writing = -sizeof(light);
/*---------------------------------------------------------------------------*/
PROCESS(cs4222_flash_demo_process, "PROCESS: Flashing external memory with data from light sensor");
PROCESS(blink_process, "PROCESS: LED blink for long press");
AUTOSTART_PROCESSES(&cs4222_flash_demo_process, &blink_process);
/*---------------------------------------------------------------------------*/
static int get_light_reading(){
  int value;
  value = opt_3001_sensor.value(0);
  if(value != CC26XX_SENSOR_READING_ERROR) {
    SENSORS_ACTIVATE(opt_3001_sensor);
    return value;
  } else {
    printf("Cannot read from light sensor\n");
    return -1;
  }
}
static void print_logo(){
  printf("\n*************************************************************************\n");
  printf("CS4222 DEMO: WRITING OPTICAL SENSOR DATA TO FLASH MEMORY , ver %s\n", version);                                                 
  printf("*************************************************************************\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(cs4222_flash_demo_process, ev, data){
  PROCESS_BEGIN();
  etimer_set(&et, CC26XX_DEMO_LOOP_INTERVAL);
  print_logo();
  SENSORS_ACTIVATE(opt_3001_sensor);
  while(1) {

    PROCESS_WAIT_EVENT();

    if(ev == PROCESS_EVENT_TIMER) {
      if(data == &et) {
        printf("nodeid,%d,light,%d.%02d\n", node_id, light/100, light%100); 
        if(write_to_ext_flash){
          leds_on(LEDS_YELLOW);

          // using external flash
          if(EXT_FLASH_BASE_ADDR_SENSOR_DATA + address_index_writing + sizeof(light) < EXT_FLASH_MEMORY_END_ADDRESS){
            int executed = ext_flash_open();
            if(!executed) {
              printf("Cannot open flash\n");
              ext_flash_close();
              return 0;
            }
            printf("WRITING TO EXTERNAL FLASH @0x%08X sizeof: %d\n",EXT_FLASH_BASE_ADDR_SENSOR_DATA + address_index_writing, sizeof(light));
            printf("%d\n", light); 
            executed = ext_flash_write(EXT_FLASH_BASE_ADDR_SENSOR_DATA + address_index_writing, sizeof(light), (uint8_t *)&light);
            if(!executed) {
              printf("Error saving data to EXT flash\n");
            }
    
            address_index_writing += sizeof(light);
            ext_flash_close();
          }else{
            printf("POSSIBLE EXTERNAL FLASH OVERFLOW @0x%08X sizeof: %d\n",EXT_FLASH_BASE_ADDR_SENSOR_DATA + address_index_writing, sizeof(light));  
          }
          leds_off(LEDS_YELLOW);
        }
        etimer_set(&et, CC26XX_DEMO_LOOP_INTERVAL);
      }
    }else if(ev == sensors_event) {
      if(data == CC26XX_BUTTON_LEFT) {
        printf("Left: Pin %d, press duration %d clock ticks\n",
        (CC26XX_BUTTON_LEFT)->value(BUTTON_SENSOR_VALUE_STATE),
        (CC26XX_BUTTON_LEFT)->value(BUTTON_SENSOR_VALUE_DURATION));

        if((CC26XX_BUTTON_LEFT)->value(BUTTON_SENSOR_VALUE_DURATION) > CLOCK_SECOND) {
          printf("Long button press!\n");
          if(!write_to_ext_flash){
            write_to_ext_flash=true;
            printf("WILL NOW WRITE TO FLASH!\n");
          }
        }
      } 
      else if(data == CC26XX_BUTTON_RIGHT) {
        printf("Right: Pin %d, press duration %d clock ticks\n",
        (CC26XX_BUTTON_RIGHT)->value(BUTTON_SENSOR_VALUE_STATE),
        (CC26XX_BUTTON_RIGHT)->value(BUTTON_SENSOR_VALUE_DURATION));
        if((CC26XX_BUTTON_RIGHT)->value(BUTTON_SENSOR_VALUE_DURATION) > CLOCK_SECOND) {
          printf("Long button press!\n");
          if(write_to_ext_flash){
            write_to_ext_flash=false;
            printf("WILL NOW STOP WRITING TO FLASH!\n");
            leds_on(LEDS_GREEN);
            blinks = 0;
          }
        }
      }else if(data == &opt_3001_sensor) {
        light = get_light_reading();
      }
    }
  }
  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(blink_process, ev, data){
  PROCESS_BEGIN();
  blinks = 0;
  while(1) {
    etimer_set(&et_blink, CLOCK_SECOND*2);
    if(!write_to_ext_flash){
      print_logo();
      printf("Long press (over 1 second) left pin to write sensor data!\n");
      printf("RED LED blinks 4 times when writing to flash is activated!\n");
      printf("*************************************************************************\n");
    }else{
      print_logo();
      printf("Long press (over 1 second) right pin to stop writing sensor data!\n");
      printf("GREEN LED stays ON when writing is stopped!\n");
      printf("*************************************************************************\n");
    }
    PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER);
    if(blinks < 8 && write_to_ext_flash){
      leds_toggle(LEDS_RED);
      blinks++;
      printf("blinks %d\n", blinks);
    }else if(blinks == 8){
        leds_off(LEDS_RED);
    }
  }
  PROCESS_END();
}
/*---------------------------------------------------------------------------*/