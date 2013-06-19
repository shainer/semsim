#!/usr/bin/python

import sys

def printUsage():
	print
	print "-- Merge Years for Google Books unigrams --"
	print "    Usage: " + sys.argv[0] + " <input file> <output file>"
	print

def mergeYears(inputFile, outputFile):
	currentWord = ""
	frequencyCount = 0
	lineCount = 1

	for line in inputFile:
		lineCount += 1
		if lineCount % 100 == 0:
			print ":: Current line " + str(lineCount)

		fields = line.split('\t');
		word = fields[0]

		if currentWord != word:
			# No more occurrences of the previous word, write the total frequency count
			# The year and books are not needed, so they are omitted here
			if frequencyCount > 0:
				outputFile.write(currentWord + "\t" + str(frequencyCount) + "\n")

			currentWord = word
			frequencyCount = 0

		# Simply add the new frequency count to the total
		frequencyCount += int(fields[2])

	inputFile.close()
	outputFile.close()

if __name__ == "__main__":
	if (len(sys.argv) != 3):
		printUsage()
		exit(0)

	inputFileName = sys.argv[1]
	outputFileName = sys.argv[2]

	try:
		inputFile = open(inputFileName, "r")
		outputFile = open(outputFileName, "w")
		mergeYears(inputFile, outputFile)
	except IOError as io:
		print io.strerror
