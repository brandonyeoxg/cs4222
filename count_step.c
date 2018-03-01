#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MAX_BUF 255

struct Vector {
    float x;
    float y;
    float z;
};

float getVecMag(struct Vector v) {
    return sqrt(pow(v.x, 2) + pow(v.y, 2) + pow(v.z, 2));
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
    return 0;
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage of this program: ./count_step team_accel.csv\n");
        return -1;
    }
    struct Vector* dataSets;
    int numLines = 0;
    dataSets = getAccelData(argv[1], &numLines);
    
    int numOfSteps = getDeadReckoningStepCount(dataSets, numLines);
    
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
        float currentVecMag = getVecMag(dataSets[i]);
        magAccumulatedSoFar += currentVecMag;
    }
    float meanOfSamples = magAccumulatedSoFar / (2 * windowRange + 1);
    return meanOfSamples;
}

float getVarianceOfSamplesInWindow(struct Vector *dataSets, int numLines, int windowRange, int start, int end) {
    int i;
    float diffAccumulatedSoFar = 0; // (aj - aj_bar)^2
    for (i = start; i <= end; i++) {
        float currentVecMag = getVecMag(dataSets[i]);
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
