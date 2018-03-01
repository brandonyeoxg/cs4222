#include "zee.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Vector getAccelMean(struct Vector *dataSets, int numLines) {
	if (numLines < 1) {
		printf("There are no lines to compute for the mean\n");
		exit(EXIT_FAILURE);
	}
	int i;
	float x = 0.0 ,y = 0.0 ,z = 0.0;
	x = y = z = 0.0;
	for(i = 0; i < numLines; ++i) {
		x += dataSets[i].x;
		y += dataSets[i].y;
		z += dataSets[i].z;
	}

	struct Vector outputVec;
	vecInit(&outputVec);

	outputVec.x = x / numLines;
	outputVec.y = y / numLines;
	outputVec.z = z / numLines;

	return outputVec;
}

struct Vector getAccelStdDev(struct Vector *dataSets, int numSamples) {
	struct Vector meanVec = getAccelMean(dataSets, numSamples);
	struct Vector stdDevVec;
	vecInit(&stdDevVec);
	int i;
	for(i = 0; i < numSamples; ++i) {
		stdDevVec.x += pow(dataSets[i].x - meanVec.x, 2);
		stdDevVec.y += pow(dataSets[i].y - meanVec.y, 2);
		stdDevVec.z += pow(dataSets[i].z - meanVec.z, 2);
	}

	stdDevVec.x = sqrt(stdDevVec.x / numSamples);
	stdDevVec.y = sqrt(stdDevVec.y / numSamples);
	stdDevVec.z = sqrt(stdDevVec.z / numSamples);

	return stdDevVec;
}

float getMean(float *dataSets, int numSamples) {
	if (numSamples < 1) {
		printf("There are no lines to compute for the mean\n");
		exit(EXIT_FAILURE);
	}	
	int i;
	float output = 0;
	for(i = 0; i < numSamples; ++i) {
		output += dataSets[i];
	}
	return output / numSamples;	
}

float getStdDev(float *dataSets, int numSamples) {
	float mean = getMean(dataSets, numSamples);
	float stdDev = 0.0f;
	int i;
	for(i = 0; i < numSamples; ++i) {
		stdDev += pow(dataSets[i] - mean, 2);
	}

	stdDev = sqrt(stdDev / numSamples);

	return stdDev;
}

/*
	Gets the mean value of samples between fromIdx to the window size
*/
struct Vector getAccelMeanFromTill(struct Vector *dataSets, int dataSetSize, int fromIdx, int windowSize) {
	int k;
	struct Vector *newSampleDataSet = (struct Vector *) malloc(sizeof(struct Vector) * windowSize);
	struct Vector outputMeanVec;
	for (k = 0; k < windowSize; ++k) {
		int targetIdx = fromIdx + k;
		newSampleDataSet[k].x = dataSets[targetIdx].x;
		newSampleDataSet[k].y = dataSets[targetIdx].y;
		newSampleDataSet[k].z = dataSets[targetIdx].z;
	}

	outputMeanVec = getAccelMean(newSampleDataSet, windowSize);
	free(newSampleDataSet);

	return outputMeanVec;
}

struct Vector getAccelStdDevFromTill(struct Vector *dataSets, int dataSetSize, int fromIdx, int windowSize) {
	int k;
	struct Vector *newSampleDataSet = (struct Vector *) malloc(sizeof(struct Vector) * windowSize);
	struct Vector outputStdDevVec;
	for (k = 0; k < windowSize; ++k) {
		int targetIdx = fromIdx + k;
		newSampleDataSet[k].x = dataSets[targetIdx].x;
		newSampleDataSet[k].y = dataSets[targetIdx].y;
		newSampleDataSet[k].z = dataSets[targetIdx].z;
	}

	outputStdDevVec = getAccelStdDev(newSampleDataSet, windowSize);
	free(newSampleDataSet);

	return outputStdDevVec;
}

float getAccelStdDevOfMag(struct Vector *dataSets, int dataSetSize, int fromIdx, int windowSize) {
	int k;
	float *newSampleMagnitude = (float *) malloc(sizeof(float) * windowSize);

	for (k = 0; k < windowSize; ++k) {
		int targetIdx = fromIdx + k;
		newSampleMagnitude[k] = vecMagnitude(dataSets[targetIdx]);
	}
	float stdDev = getStdDev(newSampleMagnitude, windowSize);
	free(newSampleMagnitude);

	return stdDev;
}

struct Vector getAutoCorrelation(struct Vector *dataSets, int dataSetSize, int m, int gamma) {
	int k;
	struct Vector meanVec;
	vecInit(&meanVec);
	struct Vector outputCorrelation;
	vecInit(&outputCorrelation);
	for(k = 0; k < gamma; ++k) {
		if (m + k + gamma >= dataSetSize) {
			break;
		}
		// (a(m + k) - mean(m, gamma)) * (a(m + k + gamma) - mean(m + gamma, gamma))
		meanVec = getAccelMeanFromTill(dataSets, dataSetSize, m, gamma);
		struct Vector left = vecMinus(dataSets[m + k], meanVec);
		
		meanVec = getAccelMeanFromTill(dataSets, dataSetSize, m + gamma, gamma);
		struct Vector right = vecMinus(dataSets[m + k + gamma], meanVec);

		struct Vector result = vecMult(left, right);
		outputCorrelation = vecAdd(outputCorrelation, result); 
	}

	struct Vector stdDevLeftVec, stdDevRightVec;
	stdDevLeftVec = getAccelStdDevFromTill(dataSets, dataSetSize, m, gamma);
	stdDevRightVec = getAccelStdDevFromTill(dataSets, dataSetSize, m + gamma, gamma);

	struct Vector denominatorVec;
	denominatorVec = vecMult(stdDevLeftVec, stdDevRightVec);
	denominatorVec = vecMultConst(denominatorVec, gamma);

	outputCorrelation = vecDiv(outputCorrelation, denominatorVec);

	return outputCorrelation;
}

struct Vector getMaxCorrelation(struct Vector *dataSets, int dataSetSize, int m, int gMin, int gMax, int *gOpt) {
	int gamma = 0;
	struct Vector highestCorrelation;
	vecInit(&highestCorrelation);
	int highestGamma = gMin;
	for (gamma = gMin; gamma < gMax + 1; ++gamma) {
		if (m + gamma + gamma >= dataSetSize) {
			break;
		}
		struct Vector correlation = getAutoCorrelation(dataSets, dataSetSize, m, gamma);

		highestCorrelation = getHighestCorrelation(highestCorrelation, correlation);
		if (vecEqual(highestCorrelation, correlation)) {
			highestGamma = gamma;
		}
	}
	*gOpt = highestGamma;
	return highestCorrelation;
}

void handleGammaWindowShift(int gOpt, int *gMin, int *gMax, int gAbsMin, int gAbsMax) {
	*gMin = gOpt - 40;
	if (*gMin < gAbsMin) {
		*gMin = gAbsMin;
		*gMax = *gMin + 80;
	} else {
		*gMax = gOpt + 40;
		if (*gMax > gAbsMax) {
			*gMax = gAbsMax;
			*gMin = *gMax - 80;
		}
	}
}