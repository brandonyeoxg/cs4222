class ActivityDetection: 
	def __init__(self):
		self.data = []
		self.activityState = 'IDLE'

	def getActivityState(self):
		return self.activityState