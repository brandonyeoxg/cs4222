class ActivityState:
	IDLE = 'IDLE'
	WALKING = 'WALKING'
	NO_FLOOR_CHANGE = "NoFloorChange"
	FLOOR_CHANGE = "FloorChange"
	INDOOR = 'Indoor'
	OUTDOOR = 'Outdoor'

class ActivityDetection: 
	def __init__(self):
		self.data = []
		self.activityState = ACtivityState.IDLE

	def getActivityState(self):
		return self.activityState

	def insertNewData(self, data_line):
		self.data.append(data_line)

	def compute(self):
		pass