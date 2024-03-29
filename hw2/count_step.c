#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MAX_BUF 255

enum STATE {IDLE = 0, WALKING = 1};

struct Vector {
	float x;
	float y;
	float z;
};

struct Vector vecAdd(struct Vector left, struct Vector right) {
	struct Vector result;
	result.x = left.x + right.x;
	result.y = left.y + right.y;
	result.z = left.z + right.z;

	return result;
}

struct Vector vecMinus(struct Vector left, struct Vector right) {
	struct Vector result;
	result.x = left.x - right.x;
	result.y = left.y - right.y;
	result.z = left.z - right.z;

	return result;
}

struct Vector vecMult(struct Vector left, struct Vector right) {
	struct Vector result;
	result.x = left.x * right.x;
	result.y = left.y * right.y;
	result.z = left.z * right.z;

	return result;
}

struct Vector vecMultConst(struct Vector vec, float constant) {
	struct Vector result;
	result.x = vec.x * constant;
	result.y = vec.y * constant;
	result.z = vec.z * constant;

	return result;
}

struct Vector vecDiv(struct Vector left, struct Vector right) {
	struct Vector result;
	result.x = left.x / right.x;
	result.y = left.y / right.y;
	result.z = left.z / right.z;

	return result;
}

int vecEqual(struct Vector left, struct Vector right) {
	if (left.x != right.x) {
		return 0;
	}
	if (left.y != right.y) { 
		return 0;
	}
	if (left.z != right.z) {
		return 0;
	}
	return 1;
}

int vecMoreThanConst(struct Vector v, float f) {
	if (v.x > f) {
		return 1;
	}
	if (v.y > f) {
		return 1;
	}
	if (v.z > f) {
		return 1;
	}
	return 0;
}

float vecMagnitude(struct Vector input) {
	return sqrt(pow(input.x, 2) + pow(input.y, 2) + pow(input.z, 2));
}

struct Vector getHighestCorrelation(struct Vector left, struct Vector right) {
	float leftSum = left.x + left.y + left.z;
	float rightSum = right.x + right.y + right.z;

	if (leftSum > rightSum) {
		return left;
	}
	return right;
}

void vecInit(struct Vector *inVec) {
	inVec->x = 0.0;
	inVec->y = 0.0;
	inVec->z = 0.0;
}

int getCSVLineCount(FILE *file);
void getAccelInfo(FILE *file, struct Vector *dataSets, int numLines);
struct Vector* getAccelData(char *filename, int *numLines);
void printDataSets(struct Vector *dataSets, int numLines);
float getMeanOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end);
float getVarianceOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end);
float *getStandardDevArray(struct Vector *dataSets, int numLines, int windowRange);
int getNumOfSteps(float *standardDevArray, int numLines, int windowRange, float thresholdReady, float thresholdRecord);

/*== To be implemented! ==*/
int getDeadReckoningStepCount(struct Vector *dataSets, int numLines) {
    printf("===Printing from Dead Reckoning===\n");
    int windowRange = 15;
    float *varianceArray = getStandardDevArray(dataSets, numLines, windowRange);
    float thresholdReady = 1.45;
    float thresholdRecord = 1.4;
    int totalSteps = getNumOfSteps(varianceArray, numLines, windowRange, thresholdReady, thresholdRecord);
    printf("Number of Steps: %d\n", totalSteps);
    
    free(varianceArray);
    return totalSteps;
}
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

int getZeeStepCount(struct Vector *dataSets, int numSamples) {
	printf("=== Starting Zee step counting ===\n");
	int gMin = 120;
	int gMax = 200;
	int gOpt = 0;
	int gAbsMin = 120, gAbsMax = 240;
	enum STATE state = IDLE;
	int numSteps = 0;
	int i;
	int numStepsCtr = 0;
	for (i = 0; i < numSamples; ++i) {
		struct Vector highestCorrelation;
		highestCorrelation = getMaxCorrelation(dataSets, numSamples, i, gMin, gMax, &gOpt);
		if (getAccelStdDevOfMag(dataSets, numSamples, i, gOpt) < 0.01) {
			state = IDLE;
			numStepsCtr = 0;
		} else if (vecMoreThanConst(highestCorrelation, 0.7)) {
			state = WALKING;
			gMin = gOpt - 40;
			if (gMin < gAbsMin) {
				gMin = gAbsMin;
				gMax = gMin + 80;
			} else {
				gMax = gOpt + 40;
				if (gMax > gAbsMax) {
					gMax = gAbsMax;
					gMin = gMax - 80;
				}
			}
		}

		if (state == IDLE) {
			continue;
		}
		if (numStepsCtr > gOpt / 2) {
			numSteps += 1;
			numStepsCtr = 0;
			continue;
		}
		numStepsCtr += 1;
	}

	return numSteps;
}

int main(int argc, char *argv[]) {
	if (argc < 2) {
		printf("Usage of this program: ./count_step team_accel.csv\n");
		return -1;
	}
	struct Vector* dataSets;
	int numLines = 0;
	dataSets = getAccelData(argv[1], &numLines);
	int numOfSteps = getZeeStepCount(dataSets, numLines);
	printf("Num of steps: %d\n", numOfSteps);
	free(dataSets);
	return 0;
}

/*
	Gets the line count of the CSV file.
*/
int getCSVLineCount(FILE *file) {
	int lines = 0;
	char c;
	for (c = getc(file); c != EOF; c = getc(file)) {
		if (c == '\n') {
			lines += 1;
		}
	}
	return lines;
}

/*
	Gets the acceleration data from the CSV file and stores it in the data struct Vector
*/
void getAccelInfo(FILE *file, struct Vector *dataSets, int numLines) {
	char buf[MAX_BUF] = {0x0};
	int i = 0;
	char* field;
	while(i < numLines) {
		fgets(buf, MAX_BUF, (FILE *)file);
		field = strtok(buf, ",");
		dataSets[i].x = atof(field);

		field = strtok(NULL, ",");
		dataSets[i].y = atof(field);

		field = strtok(NULL, ",");
		dataSets[i].z = atof(field);
		
		i += 1;
	}
}

/*
	Returns the the number of the lines of the csv file read.
	Outputs the sanitised acceleration data through the param outputDataSets as a struct Vector.
*/
struct Vector *getAccelData(char *filename, int *lines) {
	printf("Opening file: %s\n", filename);
	FILE *file = fopen(filename, "r'");
	if (file == NULL) {
		printf("File does not exist!!\n");
		exit(EXIT_FAILURE);
	}

	*lines = getCSVLineCount(file);
	
	printf("File has: %d lines\n", *lines);
	struct Vector* dataSets = (struct Vector *) malloc(sizeof(struct Vector) * (*lines));

	fseek(file, 0, SEEK_SET);
	getAccelInfo(file, dataSets, *lines);

	fclose(file);
	return dataSets;
}

float getMeanOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end) {
    int i;
    float magAccumulatedSoFar = 0;
    for (i = start; i <= end; i++) {
        float currentVecMag = vecMagnitude(dataSets[i]);
        magAccumulatedSoFar += currentVecMag;
    }
    float meanOfSamples = magAccumulatedSoFar / (2 * windowRange + 1);
    return meanOfSamples;
}

float getVarianceOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end) {
    int i;
    float diffAccumulatedSoFar = 0; // (aj - aj_bar)^2
    for (i = start; i <= end; i++) {
        float currentVecMag = vecMagnitude(dataSets[i]);
        float meanOfCurrentSampleIndex = getMeanOfSamplesInWindow(dataSets, numLines, windowRange, i, i + 2 * windowRange);
        float diffBetweenMagMean = currentVecMag - meanOfCurrentSampleIndex;
        float diffSquared = pow(diffBetweenMagMean, 2);
        diffAccumulatedSoFar += diffSquared;
    }
    return diffAccumulatedSoFar / (2 * windowRange + 1);
}

float *getStandardDevArray(struct Vector *dataSets, int numLines, int windowRange) {
    int i;
    float *arrayOfStandardDev = malloc(sizeof(float) * (numLines - (2 * windowRange))); // array index is 0, but actually is index 0 + windowRAnge
    for (i = 0; i < numLines - (2 * windowRange); i++) {
        arrayOfStandardDev[i] = sqrt(getVarianceOfSamplesInWindow(dataSets, numLines, windowRange, i, i + 2 * windowRange));
    }
    return arrayOfStandardDev;
}

int getNumOfSteps(float *standardDevArray, int numLines, int windowRange, float thresholdReady, float thresholdRecord) {
    int numOfSteps = 0;
    int state = 0; //0 for idle, 1 for ready mode
    int i;
    int temp = state;
    for (i = 0; i < numLines - (2 * windowRange); i++) {
        temp = state;
        if (standardDevArray[i] > thresholdReady) {
            state = 1;
        } else if (standardDevArray[i] < thresholdRecord) {
            if (temp == 1) { // originally in ready mode
                numOfSteps += 1;
            }
            state = 0; // Reset state to be back to idle
        }
    }
    return numOfSteps;
}
/*
	Prints the entire data set, used as a debug func.
*/
void printDataSets(struct Vector *dataSets, int numLines) {
	int i;

	for (i = 0; i < numLines; i++ ) {
		printf("Data %i: X=%.2f, Y=%.2f, Z=%.2f\n", i + 1, dataSets[i].x, dataSets[i].y, dataSets[i].z);
	}
}
