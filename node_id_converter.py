def convertNodeIdToRepresentation(nodeid):
	binstr = bin(int(nodeid))[2:].zfill(16)
	left = binstr[:8]
	right = binstr[8:]
	leftInDec = int(left, 2)
	rightInDec = int(right, 2)
	return leftInDec, rightInDec

nodeid = input("Input in node id to be converted to contiki format: ")

leftInDec, rightInDec = convertNodeIdToRepresentation(nodeid)

print("Contiki NodeId representation: %s.%s" % (leftInDec, rightInDec))