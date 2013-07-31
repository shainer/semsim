#!/usr/bin/python

#####################################################################
#                                                                   #
# Script to index a Google Corpus file, preprocessed with the       #
# mergeYears.py script, by its two initials, removing words not     #
# appearing in an English dictionary.                               #
#                                                                   #
#####################################################################

import os
import sys

def printUsage():
	print
	print "-- Divide Google Books Unigrams reduced files for fast lookup --"
	print "    Usage: " + sys.argv[0] + " <file1> ... <fileN>"
	print

def divideByInitials(fileName):
	currentInitials = ""
	currentWriteFile = None
	dictionaryFile = None
	dictionary = []
	f = open(fileName, "r")

	for line in f:
		fields = line.split("\t")
		word = fields[0]
		lowerWord = word.lower()

		actualWord = lowerWord.split(".")[0]
		actualWord = lowerWord.split("_")[0]

		if (not actualWord.isalpha() or len(actualWord) < 2):
			continue

		if currentInitials != lowerWord[0:2]:
			currentInitials = lowerWord[0:2]
			initial = lowerWord[0:1]

			try:
				d = os.path.dirname(initial + "/")
				if (not os.path.exists(d)):
					os.makedirs(d)
			except IOError as io:
				print ":: Error creating directory \"" + d + "\": " + io.strerror
				exit(-1)

			try:
				if (currentWriteFile != None):
					currentWriteFile.close()

				currentWriteFile = open(initial + "/" + currentInitials, "a+")

				dictionaryFile = open("dictionary/" + currentInitials, "r");
				dictionary = dictionaryFile.readlines()
				dictionaryFile.close()
			except IOError as io:
				print ":: Error opening file \"" + currentInitials + "\": " + io.strerror
				exit(-1)

		actualWordNewline = actualWord + "\n"
		if (not actualWordNewline in dictionary):
			continue

		try:
			currentWriteFile.write(word + "\t" + fields[1])
		except IOError as io:
			print ":: Error writing on file \"" + currentInitials + "\": " + io.strerror
			exit(-1)

	currentWriteFile.close()


if __name__ == "__main__":
	if len(sys.argv) < 2:
		printUsage() 
		exit(0)

	for fileName in sys.argv[1:]:
		divideByInitials(fileName)
