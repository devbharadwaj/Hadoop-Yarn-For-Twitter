#!/usr/bin/python 
import collections

Trends = ["#GleeAlong",\
	  "#AwkwardSeniorYear",\
	  "Obama",\
	  "China",\
	  "#JackieRobinsonDay",\
	  "USA",\
	  "Ukraine",\
	  "#himynameiscamila",\
	  "Miami",\
	  "MH370"]

dictionary = {}
with open('paircount.txt','r') as pairfile:
	for line in pairfile:
		for keyword in Trends:
			if keyword.lower() in line.lower():
				dictionary[line.split('\t')[0]+'\t'+keyword] = line.split('\t')[1]
pairfile.close()

for key in dictionary.keys():
	word = str(key).split('\t')[1]
	output = open(word+'.txt','a')
	output.write(str(key)+'\t')
	output.write(str(dictionary[key]))
	output.close()

sortedDict = {}
for keyword in Trends:
	with open(keyword+'.txt','r') as breakups:
		for line in breakups:
			sortedDict[line.split('\t')[0]] = line.split('\t')[2]
		newDict = collections.OrderedDict(sorted(sortedDict.items(),reverse=True))
		for keyvalue in newDict.items():
			sortedfile = open(keyword+'Sort.txt','a')
			sortedfile.write(str(keyvalue[0])+'\t'+str(keyvalue[1]))
			sortedfile.close()
		sortedDict = {}
		breakups.close()

