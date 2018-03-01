#include "Vector.h"
#ifndef _ZEEH_
#define _ZEEH_
enum STATE {IDLE = 0, WALKING = 1};

float getMean(float *dataSets, int numSamples);
float getStdDev(float *dataSets, int numSamples);

struct Vector getAccelMean(struct Vector *dataSets, int numLines);
struct Vector getAccelStdDev(struct Vector *dataSets, int numSamples);
struct Vector getAccelMeanFromTill(struct Vector *dataSets, int dataSetSize, int fromIdx, int windowSize);
struct Vector getAccelStdDevFromTill(struct Vector *dataSets, int dataSetSize, int fromIdx, int windowSize);
float getAccelStdDevOfMag(struct Vector *dataSets, int dataSetSize, int fromIdx, int windowSize);

struct Vector getAutoCorrelation(struct Vector *dataSets, int dataSetSize, int m, int gamma);
struct Vector getMaxCorrelation(struct Vector *dataSets, int dataSetSize, int m, int gMin, int gMax, int *gOpt);

void handleGammaWindowShift(int gOpt, int *gMin, int *gMax, int gAbsMin, int gAbsMax);
#endif