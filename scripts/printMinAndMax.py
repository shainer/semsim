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
  featureSize = 14
  
  for i in range(0, featureSize):
    maximum.append(0)
    minimum.append(25000)
    
  for line in inputFile:
    features = line.split("\t")
    features = features[:-1]
        
    for i in range(0, featureSize):
      if float(features[i]) > maximum[i]:
	maximum[i] = features[i]
      if float(features[i]) < minimum[i]:
	minimum[i] = features[i]
	

  for i in range(0, featureSize):
    outputFile.write("Max: " + str(maximum[i]) + "\n")
    outputFile.write("Min: " + str(minimum[i]) + "\n\n")
  
  inputFile.close()
  outputFile.close()