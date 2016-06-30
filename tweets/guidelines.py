#!/usr/bin/python

import sys

def indexof(line, ch):
	i = 0

	for c in line:
		if c == ch:
			return i
		i += 1

	return -1

def createList(v):
	isString = False
	res = []
	s = ""

	for ch in v:
		if ch == "\"":
			if isString:
				isString = False
				res.append(s)
				s = ""
			else:
				isString = True
		elif isString:
			s += ch

	return res


if __name__ == "__main__":
	if len(sys.argv) < 2:
		print "Filename required as input"
		sys.exit(-1)

	filename = sys.argv[1]
	f = open(filename, "r")

	desiredFields = {"tokens": [], "normalized_tokens": [], "lemmas": [], "pos": [], "senses": []}
	count = 0

	for line in f:
		i = 0
		while i < len(line) - 1 and (line[i] == ' ' or line[i] == '\t'):
			i += 1

		line = line[i:]
		j = indexof(line, ":")

		if j == -1:
			continue

		key = line[1:j-1]
		value = line[j+1:]

		if key in desiredFields:
			if len(desiredFields[key]) != 0:
				continue

			desiredFields[key] = createList(value)


	i = 0
	print "    \"tokens\": ["

	for i in range(0, len(desiredFields["tokens"])):
		print "      {"
		print "        \"lemma\": \"" + desiredFields["lemmas"][i] + "\","
		print "        \"normalized\": \"" + desiredFields["normalized_tokens"][i] + "\","
		print "        \"token\": \"" + desiredFields["tokens"][i] + "\","
		print "        \"bnsense\": \"" + desiredFields["senses"][i] + "\","

		t = ""
		if desiredFields["pos"][i] == "^":
			t = "NE"
		else:
			t = "token"

		print "        \"type\": \"" + t + "\","
		print "        \"pos\": \"" + desiredFields["pos"][i] + "\","
		print "      },"
	print "    ],"


	f.close()
