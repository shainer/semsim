#!/usr/bin/python

#####################################################################
#                                                                   #
# Script to go from the sample file format recognized by my system  #
# to the one recognized by libsvm tools such as svm-train.          #
#                                                                   #
#####################################################################

import sys

if len(sys.argv) != 3:
  print "Usage: ./toLIBSVMformat.py <input file> <output file>"
  exit(-1)

features = open(sys.argv[1], "r")
writing = open(sys.argv[2], "w")

numFeatures = 19 # remember to keep this updated if you add more features

for line in features:
  fields = line.split("\t")
  
  writing.write(fields[numFeatures][:-1]) # writes the target value as the first in line (minus final newline)
  
  for featureCount in range(0, numFeatures):
    writing.write(" " + str(featureCount+1) + ":" + fields[featureCount]) # writes features with their index

  writing.write("\n")  
    
features.close()
writing.close()