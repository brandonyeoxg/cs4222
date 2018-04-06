from activity_state import ActivityState
import math
import logging, sys
class Vector:
	import math
	def __init__(self, x = 0.0, y = 0.0, z = 0.0):
		self.x = x
		self.y = y
		self.z = z

	def getMag(self):
		return math.sqrt(math.pow(self.x, 2) + math.pow(self.y, 2) + math.pow(self.z, 2))

	def greaterThanConst(self, const):
		if (self.x > const):
			return True
		if (self.y > const):
			return True
		if (self.z > const):
			return True
		return False;

	def equal(self, vec):
		if (self.x != vec.x):
			return False
		if (self.y != vec.y):
			return False
		if (self.z != self.z):
			return False
		return True

	def add(self, vec):
		result = Vector()
		result.x = self.x + vec.x
		result.y = self.y + vec.y
		result.z = self.z + vec.z
		return result

	def minus(self, vec):
		result = Vector()
		result.x = self.x - vec.x
		result.y = self.y - vec.y
		result.z = self.z - vec.z
		return result

	def mult(self, vec):
		result = Vector()
		result.x = self.x * vec.x
		result.y = self.y * vec.y
		result.z = self.z * vec.z
		return result

	def multConst(self, const):
		result = Vector()
		result.x = self.x * const
		result.y = self.y * const
		result.z = self.z * const
		return result

	def div(self, vec):
		result = Vector()
		result.x = self.x / vec.x
		result.y = self.y / vec.y
		result.z = self.z / vec.z
		return result 

	def divConst(self, const):
		if const == 0:
			raise ZeroDivisionError("Vec.divConst: Cannot divide by 0!")
		result = Vector()
		result.x = self.x / const
		result.y = self.y / const
		result.z = self.z / const
		return result

	def __str__(self):
		return str('X: %f Y: %f Z: %f' % (self.x, self.y, self.z))

class WalkDetector:
	gMin = 20
	gMax = 60
	gOpt = 0
	gAbsMin = 15
	gAbsMax = 100

	logging.basicConfig(stream=sys.stderr, level=logging.DEBUG)

	def __init__(self, samples):
		logging.info("Walk Detector")
		self.samples = list(map(lambda x: Vector(x[0], x[1], x[2]), samples))
		logging.info('Total samples: %d' % (len(self.samples)))
	def compute(self):
		state = ActivityState.IDLE
		numStepCtr = 0
		for i in range(len(self.samples)):
			highestCorrelation = self.getMaxCorrelation(i)
			if (self.getAccelStdDevOfMag(self.samples, i, self.gOpt) < 0.01) is True:
				state = ActivityState.IDLE
			elif (highestCorrelation.greaterThanConst(0.7)) is True:
				state = ActivityState.WALKING
				self.gMin = self.gOpt - 20
				if (self.gMin < self.gAbsMin):
					self.gMin = self.gAbsMin
					self.gMax = self.gMin + 40
				else:
					self.gMax = self.gOpt + 20
					if (self.gMax > self.gAbsMax):
						self.gMax = self.gAbsMax
						self.gMin = self.gMax - 40
			if (state == ActivityState.IDLE):
				continue
			if (numStepCtr > self.gOpt / 2):
				return ActivityState.WALKING;
			numStepCtr += 1

		return ActivityState.IDLE

	def getMaxCorrelation(self, m):
		highestCorrelation = Vector()
		highestGamma = self.gMin
		for gamma in range(self.gMin, self.gMax):
			if (m + gamma + gamma) >= len(self.samples):
				break
			correlation = self.getAutoCorrelation(m, gamma)
			highestCorrelation = self.getHighestCorrelation(highestCorrelation, correlation)
			if (highestCorrelation.equal(correlation)):
				highestGamma = gamma
		self.gOpt = highestGamma
		return highestCorrelation

	def getAutoCorrelation(self, m, gamma):	
		meanVec = Vector()
		outputCorrelation = Vector()
		for k in range(gamma):
			if (m + k + gamma >= len(self.samples)):
				break;
			meanVec = self.getAccelMeanFromTill(m, gamma)
			left = self.samples[m + k].minus(meanVec)
			meanVec = self.getAccelMeanFromTill(m + gamma, gamma)
			right = self.samples[m + k + gamma].minus(meanVec)
			result = left.mult(right)
			outputCorrelation = outputCorrelation.add(result)
		
		stdDevLeftVec = self.getAccelStdDevFromTill(m, gamma)
		stdDevRightVec = self.getAccelStdDevFromTill(m + gamma, gamma)
		denominatorVec = stdDevLeftVec.mult(stdDevRightVec)
		denominatorVec = denominatorVec.multConst(gamma)

		outputCorrelation = outputCorrelation.div(denominatorVec)

		return outputCorrelation


	def getAccelStdDevFromTill(self, fromIdx, windowSize):
		newSampleDataSet = self.samples[fromIdx: fromIdx + windowSize]
		outputStdDevVec = self.getAccelStdDev(newSampleDataSet, windowSize)
		return outputStdDevVec

	def getAccelStdDev(self, samples, windowSize):
		meanVec = self.getAccelMean(samples, windowSize)
		stdDevVec = Vector()
		for i in range(windowSize):
			stdDevVec.x += math.pow(samples[i].x - meanVec.x, 2)
			stdDevVec.y += math.pow(samples[i].y - meanVec.y, 2)
			stdDevVec.z += math.pow(samples[i].z - meanVec.z, 2)
		stdDevVec.x = math.sqrt(stdDevVec.x / windowSize)
		stdDevVec.y = math.sqrt(stdDevVec.y / windowSize)
		stdDevVec.z = math.sqrt(stdDevVec.z / windowSize)
		return stdDevVec

	def getAccelMeanFromTill(self, fromIdx, windowSize):
		newSampleDataSet = []
		outputMeanVec = Vector()
		for k in range(windowSize):
			targetIdx = fromIdx + k
			newSampleDataSet.append(self.samples[targetIdx])
		outputMeanVec = self.getAccelMean(newSampleDataSet, windowSize)
		return outputMeanVec


	def getAccelMean(self, samples, windowSize):
		meanVec = Vector()
		for i in range(windowSize):
			meanVec = meanVec.add(samples[i])

		meanVec = meanVec.divConst(windowSize)
		return meanVec

	def getAccelStdDevOfMag(self, samples, fromIdx, windowSize):
		newSampleMagnitude = []
		for k in range(windowSize):
			targetIdx = fromIdx + k
			vecMag = samples[targetIdx].getMag()
			newSampleMagnitude.append(vecMag)
		stdDev = self.getStdDev(newSampleMagnitude)
		return stdDev

	def getStdDev(self, samples):
		mean = self.getMean(samples)
		stdDev = 0.0
		for i in range(len(samples)):
			stdDev += math.pow(samples[i]- mean, 2)

		stdDev = math.sqrt(stdDev / len(samples))
		return stdDev

	def getMean(self, samples):
		output = 0.0
		for i in range(len(samples)):
			output += samples[i]
		return output / len(samples)

	def getHighestCorrelation(self, left, right):
		leftSum = left.x + left.y + left.z
		rightSum = right.x + right.y + right.z
		if (leftSum > rightSum):
			return left
		return right
