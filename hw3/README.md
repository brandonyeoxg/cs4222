# cs4222 hw3
## Compiling of runicast
Run `./make_runicast` script to make `.bin` and `.elf` files for runicast  <br>

## NodeId conversion to Contiki representation
Run `python node_id_converter.py <nodeid>`. <br>

Feed in the NodeId to be converted to obtain the answer. <br>

rcv node 39431: `154.7` <br>
rcv node 28417: `111.1` <br>
rcv node 47235: `184.131` <br>
rcv node 11265: `44.1` <br>
rcv node 27648: `108.0` <br>
rcv node 41219: `161.3` <br>
rcv node 27398: `107.6` <br> 
rcv node 29575: `115.135` <br>
rcv node 22406: `87.134` <br>
rcv node 31748: `124.4` <br>

Brandon NodeID representation: `82.2`<br>

## Data collected (Morning)
Channel 13 W/ CSMA:<br>
1) 48 bytes => 14 seconds<br>

Channel 22 W/ CSMA <br>
1) 48 bytes => 14 seconds<br>

Channel 26 W/ CSMA <br>
1) 48 bytes => 14 seconds<br>
2) 8 bytes => 57 seconds<br>
3) 100 bytes => 7 seconds<br>

## Data collected (Night)
Channel 13 W/ CSMA:<br>
1) 48 bytes => 11 seconds<br>
1) 8 bytes => 50 seconds
2) 100 bytes => 7 seconds

Channel 15 W/ CSMA <br>
1) 48 bytes => 11 seconds

Channel 26 W/ CSMA <br>
1) 48 bytes => 11 seconds