#!/usr/bin/python

import sys

if __name__ == "__main__":
  if len(sys.argv) != 3:
    print "Usage: " + sys.argv[0] + " <input file> <output file>"
    sys.exit(-1)
    
  inputFile = open(sys.argv[1], "r")
  outputFile = open(sys.argv[2], "w")
  maximum = []
  minimum = []
  
  for i in range(0, 16):
    maximum.append(0)
    minimum.append(25000)
    
  for line in inputFile:
    features = line.split("\t")
    line = line[:-1]
    
    for i in range(0, 16):
      if float(line[i]) > maximum[i]:
	maximum[i] = line[i]
      if float(line[i]) < minimum[i]:
	minimum[i] = line[i]
	

  for i in range(0, 16):
    outputFile.write("Max: " + maximum[i] + "\n")
    outputFile.write("Min: " + minimum[i] + "\n\n")
  
  inputFile.close()
  outputFile.close()