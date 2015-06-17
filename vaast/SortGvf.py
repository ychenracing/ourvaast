# -*- coding: utf8 -*-
#!/usr/bin/env python
import os
import re

chrDict = {}
for i in range(1, 23):
	chrDict['chr' + str(i)] = i
chrDict['chrX'] = 23
chrDict['chrY'] = 24
chrDict['chrM'] = 25

def compare_chr(x, y):
	global chrDict
	if x == None or y == None:
		raise ValueError('line should not be None!')
	return cmp(chrDict.get(x), chrDict.get(y))

def line_compare(x, y):
	if x == None or y == None:
		raise ValueError('line should not be None!')
	xFeature = re.split(r'\s+', str(x))
	yFeature = re.split(r'\s+', str(y))
	result = compare_chr(xFeature[0], yFeature[0])
	if result != 0:
		return result
	else:
		return cmp(int(xFeature[3]), int(yFeature[3]))

def sort(objectPath):
	print objectPath
	allFiles = os.listdir(objectPath)
	gvfFileNames = [x for x in allFiles if cmp(os.path.splitext(x)[1], '.gvf') == 0]
	writeString = []

	for gvfFileName in gvfFileNames:
		writeString = []
		print 'processing ', gvfFileName
		fr = open(os.path.join(objectPath, gvfFileName), 'r')
		lines = fr.readlines() 
		startLine = int(0)
		for k in range(len(lines)):
			assert lines[k] != None, 'this line is None!'
			if not cmp(lines[k][0], '#'):
				writeString.append(lines[k])
			else:
				startLine = k
				break
		lines = lines[startLine:]
		lines.sort(line_compare)
		fr.close()
		fw = open(os.path.join(objectPath, gvfFileName), 'w')
		fw.writelines(writeString)
		fw.writelines(lines) 
		fw.close()
	print 'done!'

currentPath = os.getcwd()
casePath = os.path.join(currentPath, 'case')
concentrationList = os.listdir(casePath)
flag = False 


flag = True
if flag:
	for concentration in concentrationList:
		sort(os.path.join(casePath, concentration))
	sort(os.path.join(currentPath, 'control'))
else:
	sort(currentPath)
