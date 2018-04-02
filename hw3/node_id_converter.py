import sys

def convertNodeIdToRepresentation(nodeid):
	binstr = bin(int(nodeid))[2:].zfill(16)
	left = binstr[:8]
	right = binstr[8:]
	leftInDec = int(left, 2)
	rightInDec = int(right, 2)
	return leftInDec, rightInDec

if len(sys.argv) is 2 :
	nodeid = sys.argv[1]
	leftInDec, rightInDec = convertNodeIdToRepresentation(nodeid)
	print("Contiki NodeId representation: %s.%s" % (leftInDec, rightInDec))
else :
	print("Proper usage: python node_id_converter <nodeid>")