#!/usr/bin/python

import sys

if __name__ == "__main__":
	totalCounts = open("googlebooks-eng-all-totalcounts-20120701.txt", "r")
	content = totalCounts.read()
	totalCounts.close()
	totalMatchCount = 0

	for yearCount in content.split("\t"):
		if len(yearCount) > 0 and yearCount != ' ':
			print "\"" + yearCount + "\""
			yearFields = yearCount.split(",")
			totalMatchCount += int(yearFields[1])

	print totalMatchCount