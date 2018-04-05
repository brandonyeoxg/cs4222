import numpy

class WalkDetector:
	IDLE = 0
	WALKING = 1
	gMin = 120
	gMax = 200
	gOpt = 0
	gAbsMin = 120
	gAbsMax = 240

	def __init__(self, samples):
		self.samples = samples 

	def getStepDetection(self):
		state = IDLE
		numStepCtr = 0
		for i in range(len(samples)):
			highestCorrelation = getMaxCorrelation()
			if (getStdDevOfMag() < 0.01) is True:
				state = IDLE
			elif (isVecMoreThanConst(highestCorrelation, 0.7)) is True:
				state = WALKING
				self.gMin = self.gOpt - 40
				if (self.gMin < self.gAbsMin):
					self.gMin = self.gAbsMin
					self.gMax = self.gMin + 80
				else:
					self.gMax = self.gOpt + 40
					if (self.gMax > self.gAbsMax):
						self.gMax = self.gAbsMax
						self.gMin = self.gMax - 80
			if (state == IDLE):
				continue
			if (numStepsCtr > gOpt / 2):
				return True;
			numStepCtr += 1

		return False

	def getMaxCorrelation(self, index):
		highestCorrelation = []
		highestGamma = self.gMin
		for gamma in range(self.gMin, self.gMax):
			if (index + gamma + gamma) >= len(self.samples):
				break
			correlation = getAutoCorrelation()
			highestCorrelation = getHighestCorrelation(highestCorrelation, correlation)
			if (vecEqual(highestCorrelation, correlation)):
				highestGamma = gamma
		self.gOpt = highestGamma
		return highestCorrelation

	def getStdDevOfMag(self):
		pass

	def isVecMoreThanConst(self, vec, const):
		pass

	def getAutoCorrelation(self):
		pass

	def getHighestCorrelation(self, left, right):
		leftSum = left[0] + left[1] + left[2]
		rightSum = right[0] + right[1] + right[2]
		if (leftSum > rightSum):
			return left
		return right

	def vecEqual(self, left, right):
		if (left[0] != right[0]):
			return False
		if (left[1] != right[1]):
			return False
		if (left[2] != right[2]):
			return False
		return True
