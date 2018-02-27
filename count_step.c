#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MAX_BUF 255
#define BRANDON

enum STATE {IDLE = 0, WALKING =1};

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
	if (v.x <= f) {
		return 0;
	}
	if (v.y <= f) {
		return 0;
	}
	if (v.z <= f) {
		return 0;
	}
	return 1;
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

int getCSVLineCount(FILE *file);
void getAccelInfo(FILE *file, struct Vector *dataSets, int numLines);
struct Vector* getAccelData(char *filename, int *numLines);
void printDataSets(struct Vector *dataSets, int numLines);

/*
== To be implemented! ==
@param *dataSets represents the array of datasets that we have stored
@param numLines represents the total number of the datasets in the CSV file - use this to find the max element in the dataSet
@return the total number of step count based on the implementation
*/
int getDeadReckoningStepCount(struct Vector *dataSets, int numLines) {
	printf("===Printing from Dead Reckoning===\n");
	printDataSets(dataSets, numLines);
	return 0;
}

struct Vector *getAccelMean(struct Vector *dataSets, int numLines) {
	if (numLines < 1) {
		printf("There are no lines to compute for the mean\n");
		exit(EXIT_FAILURE);
	}
	int i;
	float x ,y,z;
	x = y = z = 0.0;
	for(i = 0; i < numLines; ++i) {
		x += dataSets[i].x;
		y += dataSets[i].y;
		z += dataSets[i].z;
	}

	struct Vector *outputVec = (struct Vector *) malloc(sizeof(struct Vector));

	outputVec->x = x / numLines;
	outputVec->y = y / numLines;
	outputVec->z = z / numLines;

	return outputVec;
}

struct Vector *getAccelStdDev(struct Vector *dataSets, int numSamples) {
	struct Vector *meanVec = getAccelMean(dataSets, numSamples);
	struct Vector *stdDevVec = (struct Vector *) malloc(sizeof(struct Vector));
	int i;
	for(i = 0; i < numSamples; ++i) {
		stdDevVec->x += pow(dataSets[i].x - meanVec->x, 2);
		stdDevVec->y += pow(dataSets[i].y - meanVec->y, 2);
		stdDevVec->z += pow(dataSets[i].z - meanVec->z, 2);
	}
	free(meanVec);

	stdDevVec->x = sqrt(stdDevVec->x / numSamples);
	stdDevVec->y = sqrt(stdDevVec->y / numSamples);
	stdDevVec->z = sqrt(stdDevVec->z / numSamples);

	return stdDevVec;
}

/*
	Gets the mean value of samples between fromIdx to the window size
*/
struct Vector *getAccelMeanFromTill(struct Vector *dataSets, int fromIdx, int windowSize) {
	int k;
	struct Vector *newSampleDataSet = (struct Vector *) malloc(sizeof(struct Vector) * windowSize);
	struct Vector *outputMeanVec = (struct Vector *) malloc(sizeof(struct Vector));
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

struct Vector *getAccelStdDevFromTill(struct Vector *dataSets, int fromIdx, int windowSize) {
	int k;
	struct Vector *newSampleDataSet = (struct Vector *) malloc(sizeof(struct Vector) * windowSize);
	struct Vector *outputStdDevVec = (struct Vector *) malloc(sizeof(struct Vector));
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

struct Vector *getAutoCorrelation(struct Vector *dataSets, int m, int gamma) {
	int k;
	struct Vector *meanVec;
	struct Vector *outputCorrelation = (struct Vector *) malloc(sizeof(struct Vector *));
	for(k = 0; k < gamma; ++k) {

		// (a(m + k) - mean(m, gamma)) * (a(m + k + gamma) - mean(m + gamma, gamma))
		meanVec = getAccelMeanFromTill(dataSets, m, gamma);
		struct Vector left = vecMinus(dataSets[m + k], *meanVec);
		meanVec = getAccelMeanFromTill(dataSets, m + gamma, gamma);
		struct Vector right = vecMinus(dataSets[m + k + gamma], *meanVec);

		struct Vector result = vecMult(left, right);
		*outputCorrelation = vecAdd(*outputCorrelation, result); 
	}
	free(meanVec);

	struct Vector *stdDevLeftVec, *stdDevRightVec;
	stdDevLeftVec = getAccelStdDevFromTill(dataSets, m, gamma);
	stdDevRightVec = getAccelStdDevFromTill(dataSets, m + gamma, gamma);

	struct Vector denominatorVec;
	denominatorVec = vecMult(*stdDevLeftVec, *stdDevRightVec);
	denominatorVec = vecMultConst(denominatorVec, gamma);

	*outputCorrelation = vecDiv(*outputCorrelation, denominatorVec);
	free(stdDevLeftVec);
	free(stdDevRightVec);

	return outputCorrelation;
}

struct Vector getMaxCorrelation(struct Vector *dataSets, int m, int gMin, int gMax, int *gOpt) {
	int gamma;
	struct Vector highestCorrelation;
	int highestGamma = gMin;
	for (gamma = gMin; gamma < gMax+1; ++gamma) {
		struct Vector *correlation = getAutoCorrelation(dataSets, m, gamma);
		highestCorrelation = getHighestCorrelation(highestCorrelation, *correlation);
		if (vecEqual(highestCorrelation, *correlation)) {
			highestGamma = gamma;
		}
	}
	*gOpt = highestGamma;
	return highestCorrelation;

}

int getZeeStepCount(struct Vector *dataSets, int numLines) {
	int gMax = 40;
	int gMin = 100;
	int gOpt = gMin;
	enum STATE state = IDLE;
	int numSteps = 0;
	int i;
	int numStepsCtr = 0;
	for (i = 0; i < numLines; ++i) {
		struct Vector highestCorrelation;
		highestCorrelation = getMaxCorrelation(dataSets, i, gMin, gMax, &gOpt);
		if (vecMagnitude(dataSets[i]) < 0.01) {
			state = IDLE;
			numStepsCtr = 0;
		} else if (vecMoreThanConst(highestCorrelation, 0.7)) {
			state = WALKING;
		}

		if (state == IDLE) {
			continue;
		}
		if (numStepsCtr > gOpt / 2) {
			numSteps+=1;
			numStepsCtr = 0;
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
	printDataSets(dataSets, numLines);
#ifdef BRANDON
	int numOfSteps = getZeeStepCount(dataSets, numLines);
#else 
	int numOfSteps = getDeadReckoningStepCount(dataSets, numLines);
#endif
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

/*
	Prints the entire data set, used as a debug func.
*/
void printDataSets(struct Vector *dataSets, int numLines) {
	int i;

	for (i = 0; i < numLines; i++ ) {
		printf("Data %i: X=%.2f, Y=%.2f, Z=%.2f\n", i + 1, dataSets[i].x, dataSets[i].y, dataSets[i].z);
	}
}