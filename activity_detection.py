class ActivityState:
	IDLE = 'IDLE'
	WALKING = 'WALKING'
	NO_FLOOR_CHANGE = "NOFLOORCHANGE"
	FLOOR_CHANGE = "FLOORCHANGE"
	INDOOR = 'INDOOR'
	OUTDOOR = 'OUTDOOR'
	DUMMY = 'DUMMY'

class ActivityDetection: 
	def __init__(self):
		self.data = []
		self.activityState = {ActivityState.NO_FLOOR_CHANGE, ActivityState.INDOOR, ActivityState.IDLE}
		self.prevActivityState = {ActivityState.NO_FLOOR_CHANGE, ActivityState.INDOOR, ActivityState.IDLE}
		self.timeStamp = 0
		self.newActivityChange = ActvityState.DUMMY

	def getActivityState(self):
		return self.activityState

	def insertNewData(self, data_line):
		self.data.append(data_line)

	def getData(self):
		return self.data

	def compute(self):
		# split data into multiple chunks
		for dataElement in self.data:
			sanitisedData = dataElement.split(',')
			print('Data len: %d and data is ' % (len(sanitisedData)))
			# compute based on the
			for element in sanitisedData:
				print element, 


	def isFloorChange():
		False

	def isWalking():
		False

	def isIndoor():
		False

	def __str__(self):
		return str(self.timeStamp) + ',' + self.newActivityChange