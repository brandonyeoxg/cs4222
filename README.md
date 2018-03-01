# cs4222
## General
Erasing the data can be done with: `cc2650-erase-ext-flash.bin` <br>
Reading the data can be done with: `hw2-read.bin`<br>
Sensesing of the acceleration data can be done with: `cs4222demo-cc2650-accel-flash.bin` <br>

The acceleration sampling code to find highest frequency can be edited in: `hw2.c` <br>
The acceleration sampling code to find highest frequency can be compiled using: `./make-hw2` <br>

Highest Sampling Rate: `198Hz` at `Clock Time: 0.001`<br>


## Converting flash memory txt file to csv
The helper python program is: `formatHelper.py`<br>
> After saving the flash memory to a txt file, `cat /dev/ttyACM0 > example.txt`, use `python formatHelper.py` when in Terminal under the same directory as the `example.txt`. Answer the prompts as asked.
> 
>> (1) Enter the file name: (in this case) `example.txt`
>>
>> (2) Is the sensor tag fixed or in motion: (Answer F/M) F for fixed, M for in motion.
>
> The file created would be in the same directory with the name `nodeID(F/M).csv`.


## Step counting
The code for step counting is: `count_step.c`and it is located in the `algo folder` <br>
> To compile just use the script: cd into the algo folder and type the following
>
> rm ./count_step
>
> make
>
> The usage of the program is: ./count_step ..\\csv\<csv file\>.csv.
