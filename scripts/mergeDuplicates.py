#!/usr/bin/python

import os
import sys

def deleteDuplicates(fpath):
	f = open(fpath, "r")
	dictionary = {}

	for line in f:
		fields = line.split("\t")
		word = fields[0]
		fr = fields[1]
		frequencyCount = int(fr[:-1])

		if word in dictionary:
			dictionary[word] += frequencyCount
		else:
			dictionary[word] = frequencyCount

	f.close()
	f = open(fpath, "w")

	

if __name__ == "__main__":
	if len(sys.argv) < 2:
		exit(0)

	directoryName = sys.argv[1]
	for fileName in os.listdir(directoryName):
		deleteDuplicates(directoryName + fileName)
