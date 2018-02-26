# cs4222
## General
Erasing the data can be done with: `cc2650-erase-ext-flash.bin` <br>
Reading the data can be done with: `hw2-read.bin`<br>
Sensesing of the acceleration data can be done with: `cs4222demo-cc2650-accel-flash.bin` <br>

The acceleration sampling code to find highest frequency can be edited in: `hw2.c` <br>
The acceleration sampling code to find highest frequency can be compiled using: `./make-hw2` <br>

Highest Sampling Rate: `198Hz` at `Clock Time: 0.001`<br>

## Step counting
The code for step counting is: `count_step.c`<br>
> To compile just use the script: ./make\_count\_step or gcc count\_step.c -o count\_step.
>
> The usage of the program is: ./count_step \<csv file\>.csv.

