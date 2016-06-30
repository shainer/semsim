#!/usr/bin/python

#####################################################################
#                                                                   #
# Script to go from the sample file format provided by Semeval and  #
# to the one recognized by my implementation.                       #
#                                                                   #
#####################################################################

import sys

if __name__ == "__main__":
	if len(sys.argv) < 4:
		print ":: Usage: " + sys.argv[0] + " <sentence file> <target file> <output file>"
		exit(-1)

	sentenceFile = open(sys.argv[1], "r")
	targetFile = open(sys.argv[2], "r")
	outputFile = open(sys.argv[3], "w")

	while True:
		sentence = sentenceFile.readline()
		target = targetFile.readline()

		if len(sentence) == 0 or len(target) == 0:
			break

		sentence = sentence[:-1]
		target = target[:-1]

		outputFile.write(sentence + "\t" + target + "\n")

	sentenceFile.close()
	targetFile.close()
	outputFile.close()
