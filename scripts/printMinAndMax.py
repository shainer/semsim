#!/usr/bin/python

import sys

if __name__ == "__main__":
  if len(sys.argv) != 3:
    print "Usage: " + sys.argv[0] + " <input file> <output file>"
    sys.exit(-1)
    
  inputFile = open(sys.argv[1], "r")
  outputFile = open(sys.argv[2], "w")
  maximum = []
  featureSize = 16
  
  for i in range(0, featureSize):
    maximum.append(0)
    
  for line in inputFile:
    features = line.split("\t")
    features = features[:-1]
        
    for i in range(0, featureSize):
      if float(features[i]) > maximum[i]:
	maximum[i] = float(features[i])
	

  for i in range(0, featureSize):
    if (maximum[i] < 1.0):
      maximum[i] = 1.0
    outputFile.write(str(maximum[i]) + "\n")
  
  inputFile.close()
  outputFile.close()