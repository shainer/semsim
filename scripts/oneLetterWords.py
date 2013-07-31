#!/usr/bin/python

#####################################################################
#                                                                   #
# Script to deal with one-letter words in the corpus, ignored by    #
# the indexing provided by divideByInitials.py                      #
#                                                                   #
#####################################################################

import sys

def findOneLetterWords(path, oneLetterFile):
	p = open(path, "r")

	for line in p:
		fields = line.split("\t")
		pieces = fields[0].split("_")
		word = pieces[0]
		rest = ''.join(pieces[1:])

		if len(word) == 1 and rest.isupper():
			oneLetterFile.write(word.lower() + "_" + rest + "\t" + fields[1])

	p.close()

if __name__ == "__main__":
	oneLetterFile = open("oneLetter", "w")

	for path in sys.argv[1:]:
		findOneLetterWords(path, oneLetterFile)
		print ":: File " + path + " completed"

	oneLetterFile.close()