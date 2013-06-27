#!/usr/bin/python

from nltk.corpus import wordnet as wn
import sys

if __name__ == "__main__":
	if len(sys.argv) < 2:
		print ":: Usage: " + sys.argv[0] + " <word>"
		exit(-1)

	synsets = wn.synsets( sys.argv[1] )
	for synset in synsets:
		print str(synset) + ": " + synset.definition

	lm = raw_input("Choose a lemma: ")
	print wn.lemma(lm).key