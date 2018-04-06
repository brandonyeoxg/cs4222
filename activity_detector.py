import copy
from walk_detector import WalkDetector
from indoor_detector import IndoorDetector
from floor_detector import FloorDetector
from activity_state import ActivityState

class ActivityDetector: 
	FLOOR_ACTIVITY = 0
	INDOOR_ACTIVITY = 1
	WALK_ACTIVITY = 2

	ACCEL_WINDOW = 100 # this is some arbitrary number

	def __init__(self):
		self.data = []
		self.accelData = []
		self.accelWindow = 0
		self.tempData = []
		self.tempWindow = 0
		self.baroData = []
		self.baroWindow = 0
		self.lightData = []
		self.lightWindow = 0
		self.humidData = []
		self.humidwindow = 0

		self.activityState = [ 	ActivityState.NO_FLOOR_CHANGE, 
								ActivityState.INDOOR, 
								ActivityState.IDLE ]
		self.prevActivityState = [ 	ActivityState.NO_FLOOR_CHANGE, 
									ActivityState.INDOOR, 
									ActivityState.IDLE ]
		self.activityChanged = ActivityState.DUMMY

		self.timeStamp = 0

	def getActivityState(self):
		return self.activityState

	def insertNewData(self, data_line):
		DATA_POSITION = 2
		data_line = data_line.rstrip()
		self.data.append(data_line)
		# split into multiple chunks here
		dataElements = data_line.split(',')
		print("Data that is being split")
		self.timeStamp = int(dataElements[0])
		# checks the type of the data
		print("Printing data elements")
		dataElements = list(filter(lambda x: x != '', dataElements))
		self.storeIntoCorrespondingDataList(dataElements[1], [float(i) for i in dataElements[DATA_POSITION:]])

	def storeIntoCorrespondingDataList(self, sensorType, dataPayload):
		if sensorType is 'a':
			self.accelData.append(dataPayload)
		elif sensorType is 't':
			self.tempData.append(dataPayload)
		elif sensorType is 'b':
			self.baroData.append(dataPayload)
		elif sensorType is 'l':
			self.lightData.append(dataPayload)
		elif sensorType is 'h':
			self.humidData.append(dataPayload)
		else:
			raise NameError('No sensor type found!')

	# Computes and print the output if there is a change in activity
	def computeAndOutputIfActivityChanged(self):
		# We should only run the checking after every 1 second or after a single window

		# floorDetector = FloorDetector()
		# indoorDetector = IndoorDetector()
		# activityState[FLOOR_ACTIVITY] = self.FloorDetector.compute()
		# activityState[INDOOR_ACTIVITY] = self.IndoorDetector.compute()
		if (len(self.accelData) >= self.ACCEL_WINDOW):
			print("Start computation of the accelerate data")
			walkDetector = WalkDetector(self.accelData)
			self.activityState[self.WALK_ACTIVITY] = walkDetector.compute()
			del self.accelData[:]
		# if getActivityIfChanged():
		# 	prevActivityState = copy.deepcopy(activityState)
		# 	outputActivityState()


	def getActivityIfChanged(self):
		if activityState[FLOOR_ACTIVITY] != prevActivityState[FLOOR_ACTIVITY]:
			self.activityChanged = activityState[FLOOR_ACTIVITY]
			return True
		elif activityState[INDOOR_ACTIVITY] != prevActivityState[INDOOR_ACTIVITY]:
			self.activityChanged = activityState[INDOOR_ACTIVITY]
			return True
		elif activityState[WALK_ACTIVITY] != prevActivityState[WALK_ACTIVITY]:
			self.activityChanged = activityState[WALK_ACTIVITY]
			return True
		return False

	def outputActivityState(self):
		print("%d,%s" % (self.timeStamp, self.activityChanged))

	def __str__(self):
		return str(self.timeStamp) + ',' + self.activityChanged

	def printDebugList(self):
		print("Printing out accel list")
		for element in self.accelData:
			print('x: %f y:%f z:%f' % (float(element[0]), float(element[1]), float(element[2])))
			self.accelWindow += 1
		print("Printing out baro list")
		for element in self.baroData:
			print('pA: %f' % (float(element)))
			self.baroWindow += 1
		print("Printing out temp list")
		for element in self.tempData:
			print('temp: %f' % (float(element)))
			self.tempWindow += 1
		print("Printing out light list")	
		for element in self.lightData:
			print('lux: %f' % (float(element)))
			self.lightWindow += 1
		print("Printing out humid list")
		for element in self.humidData:
			print('humid: %f' % (float(element)))
			self.humidWindow += 1