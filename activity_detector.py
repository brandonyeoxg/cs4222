import copy
from walk_detector import WalkDetector
from indoor_detector import IndoorDetector
from floor_detector import FloorDetector
from activity_state import ActivityState
import logging, sys
class ActivityDetector:
	logging.basicConfig(stream=sys.stderr, level=logging.DEBUG)
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
		logging.info(self.activityState)

	def getActivityState(self):
		return self.activityState

	def insertNewData(self, data_line):
		DATA_POSITION = 2
		data_line = data_line.rstrip()
		self.data.append(data_line)
		# split into multiple chunks here
		dataElements = data_line.split(',')
		self.timeStamp = int(dataElements[0])
		# checks the type of the data
		dataElements = list(filter(lambda x: x != '', dataElements))
		self.storeIntoCorrespondingDataList(dataElements[1], [float(i) for i in dataElements[DATA_POSITION:]], int(dataElements[0]))

	def storeIntoCorrespondingDataList(self, sensorType, dataPayload, timeStamp):
		dataPayload.append(timeStamp)
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
			# logging.info(str("Start computation of the accelerate data of len: %d" % (len(self.accelData))))
			walkDetector = WalkDetector(self.accelData)
			self.activityState[self.WALK_ACTIVITY], idxReadTill = walkDetector.compute()
			# logging.info(self.activityState)
			# logging.info('Timestamp: %d IdxReadTill: %d' % (self.timeStamp, idxReadTill))				
			self.accelData = self.accelData[20:]
		if self.getActivityIfChanged():
			self.prevActivityState = copy.deepcopy(self.activityState)
			self.outputActivityState()


	def getActivityIfChanged(self):
		if self.activityState[self.FLOOR_ACTIVITY] != self.prevActivityState[self.FLOOR_ACTIVITY]:
			self.activityChanged = self.activityState[FLOOR_ACTIVITY]
			return True
		elif self.activityState[self.INDOOR_ACTIVITY] != self.prevActivityState[self.INDOOR_ACTIVITY]:
			self.activityChanged = self.activityState[INDOOR_ACTIVITY]
			return True
		elif self.activityState[self.WALK_ACTIVITY] != self.prevActivityState[self.WALK_ACTIVITY]:
			self.activityChanged = self.activityState[self.WALK_ACTIVITY]
			return True
		return False

	def outputActivityState(self):
		print("%d,%s" % (self.timeStamp, self.activityChanged))

	def __str__(self):
		return str(self.timeStamp) + ',' + self.activityChanged

	def printDebugList(self):
		walkDetector = WalkDetector(self.accelData)
		walkDetector.compute()		
		print('Len of accelData: %d' % (len(self.accelData)))
		print('Len of baroData: %d' % (len(self.baroData)))
		print('Len of tempData: %d' % (len(self.tempData)))
		print('Len of lightData: %d' % (len(self.lightData)))
		print('Len of humidData: %d' % (len(self.humidData)))
		# print("Printing out accel list")
		# for element in self.accelData:
		# 	print('x: %f y:%f z:%f' % (float(element[0]), float(element[1]), float(element[2])))
		# 	self.accelWindow += 1
		# print("Printing out baro list")
		# for element in self.baroData:
		# 	print('pA: %f' % (float(element)))
		# 	self.baroWindow += 1
		# print("Printing out temp list")
		# for element in self.tempData:
		# 	print('temp: %f' % (float(element)))
		# 	self.tempWindow += 1
		# print("Printing out light list")	
		# for element in self.lightData:
		# 	print('lux: %f' % (float(element)))
		# 	self.lightWindow += 1
		# print("Printing out humid list")
		# for element in self.humidData:
		# 	print('humid: %f' % (float(element)))
		# 	self.humidWindow += 1