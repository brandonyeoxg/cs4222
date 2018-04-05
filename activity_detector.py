class ActivityState:
	IDLE = 'IDLE'
	WALKING = 'WALKING'
	NO_FLOOR_CHANGE = "NOFLOORCHANGE"
	FLOOR_CHANGE = "FLOORCHANGE"
	INDOOR = 'INDOOR'
	OUTDOOR = 'OUTDOOR'
	DUMMY = 'DUMMY'

class ActivityDetector: 
	def __init__(self):
		self.data = []
		self.accelData = []
		self.tempData = []
		self.baroData = []
		self.lightData = []
		self.humidData = []

		self.activityState = {ActivityState.NO_FLOOR_CHANGE, ActivityState.INDOOR, ActivityState.IDLE}
		self.prevActivityState = {ActivityState.NO_FLOOR_CHANGE, ActivityState.INDOOR, ActivityState.IDLE}
		self.timeStamp = 0
		self.newActivityChange = ActivityState.DUMMY

	def getActivityState(self):
		return self.activityState

	def insertNewData(self, data_line):
		self.data.append(data_line)
		# split into multiple chunks here
		dataElements = data_line.split(',')
		print("Data that is being split")
		for data in dataElements:
			print(data)
		# checks the type of the data
		self.storeIntoCorrespondingDataList(dataElements[1], data_line)

	def storeIntoCorrespondingDataList(self, sensorType, dataPayload):
		DATA_POSITION = 2
		if sensorType is 'a':
			self.accelData.append(dataPayload[DATA_POSITION:])
		elif sensorType is 't':
			self.tempData.append(dataPayload[DATA_POSITION])
		elif sensorType is 'b':
			self.baroData.append(dataPayload[DATA_POSITION])
		elif sensorType is 'l':
			self.lightData.append(dataPayload[DATA_POSITION])
		elif sensorType is 'h':
			self.humidData.append(dataPayload[DATA_POSITION])
		else:
			raise NameError('No sensor type found!')

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

	def printDebugList(self):
		print("Printing out accel list")
		for element in self.accelData:
			print('x: %f y:%f z:%f' % (float(element[0]), float(element[1]), float(element[2])))
		print("Printing out baro list")
		for element in self.baroData:
			print('pA: %f' % (float(element)))
		print("Printing out temp list")
		for element in self.tempData:
			print('temp: %f' % (float(element)))
		print("Printing out light list")	
		for element in self.lightData:
			print('lux: %f' % (float(element)))
		print("Printing out humid list")
		for element in self.humidData:
			print('humid: %f' % (float(element)))