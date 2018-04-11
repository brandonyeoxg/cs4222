import os
import re
import sys

file = sys.argv[1]

type = "test_converted.csv"
string = open(file).read();

final = open(str(type), 'a')

def insertExtra(line):
    return ',' + line + '\"'

def stripNewLine(line):
    return line.rstrip('\n')

def main():
    with open(file) as openfileobject:
        for line in openfileobject:
            perLine = stripNewLine(line)
            
            addition = insertExtra(perLine)
#            print(addition)
            final.write(addition + "\n")
    print("DONE")

main()
