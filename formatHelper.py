import os
import re

file = raw_input("Enter file name: ")
type = raw_input("Is the sensor tag fixed or in motion: (Answer F/M) ")
string = open(file).read()

def getNodeID():
    global string
    global nodeID
    new_str = string.split("Node ID: ")[1]
    temp = new_str.split(" ")[0]
    nodeID = temp.split()[0]
#    print nodeID
    return nodeID

def removeIntroduction():
    # Remove the top clunk of the file
    global string
    new_str = string[string.find("Time"):]
    newFile = "Without top.txt"
    open(newFile, 'w').write(new_str)
    
    return newFile

def getXValue(line):
    lineX = line.split("Acc X:")[1].split(',')[0]
    return lineX

def getYValue(line):
    lineY = line.split("Acc Y:")[1].split(',')[0]
    return lineY

def getZValue(line):
    lineZ = line.split("Acc Z:")[1]
    return lineZ

def getValues(line):
    x,y,z = getXValue(line), getYValue(line), getZValue(line)
    return x,y,z

def getTimeSlice(line):
    timeSlice = line.split("Time slice:")[1].split(',')[0]
    return timeSlice

def rejectLine(line):
    boolean = False
    if (getTimeSlice(line) == "-1"):
        boolean = True
    return boolean

nodeID = getNodeID()
final = open(str(nodeID) + str(type.upper()) + '.csv', 'a')

def main():
    with open(removeIntroduction()) as openfileobject:
        for line in openfileobject:
            if not rejectLine(line):
                x,y,z = getValues(line)
                final.write(str(x) + ', ' + str(y) + ',' + str(z))
    print("DONE")

main()
os.remove("Without top.txt")




