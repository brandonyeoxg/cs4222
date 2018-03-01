#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "Vector.h"
#include "zee.h"
#define MAX_BUF 255

int getCSVLineCount(FILE *file);
void getAccelInfo(FILE *file, struct Vector *dataSets, int numLines);
struct Vector* getAccelData(char *filename, int *numLines);
void printDataSets(struct Vector *dataSets, int numLines);
float getMeanOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end);
float getVarianceOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end);
float *getStandardDevArray(struct Vector *dataSets, int numLines, int windowRange);
int getNumOfSteps(float *standardDevArray, int numLines, int windowRange, float thresholdReady, float thresholdRecord);

/*== Gets the number of step count using the Dead Reckoning Algorithm (NOT USED IN THE RUNNING OF COUNT STEP PROGRAM) ==*/
int getDeadReckoningStepCount(struct Vector *dataSets, int numLines);
/*== Gets the number of step count using the Zee algorithm (USED IN THE RUNNING OF THE COUNT STEP PROGRAM==*/
int getZeeStepCount(struct Vector *dataSets, int numSamples);

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
	Gets the step count using the Zee algorithm.
	Note: The functions used in here can be found in zee.c

	Finds the highest correlation with the gamma window done with gMin and gMax.
	Checks if correlation is > 0.7, if it is we are in WALKING state.
	If the standard deviation of the sample's magnitude is < 0.01, we are in IDLE state.
	Steps are counted if state == WALKING and we are in the (gamma/2) iteration since WALKING STATE.
*/
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
			handleGammaWindowShift(gOpt, &gMin, &gMax, gAbsMin, gAbsMax);
		}
		if (state == IDLE) {
			continue;
		}
		if (numStepsCtr > gOpt / 2) {
			numSteps += 1;
			numStepsCtr = 0;
			printf("*** Step Detected: Sample=%d/%d, gamma = %d, gammaMin = %d, gammMax = %d, cur_steps = %d ***\n", i, numSamples, gOpt, gMin, gMax, numSteps);
			continue;
		}
		numStepsCtr += 1;
	}

	return numSteps;
}

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
	Prints the entire data set, used as a debug func.
*/
void printDataSets(struct Vector *dataSets, int numLines) {
	int i;

	for (i = 0; i < numLines; i++ ) {
		printf("Data %i: X=%.2f, Y=%.2f, Z=%.2f\n", i + 1, dataSets[i].x, dataSets[i].y, dataSets[i].z);
	}
}
