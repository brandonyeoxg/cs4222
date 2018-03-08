#include <math.h>
#ifndef _VECTORH_
#define _VECTORH_
struct Vector {
	float x;
	float y;
	float z;
};

void vecInit(struct Vector *inVec);
struct Vector vecAdd(struct Vector left, struct Vector right);
struct Vector vecMinus(struct Vector left, struct Vector right);
struct Vector vecMult(struct Vector left, struct Vector right);
struct Vector vecMultConst(struct Vector vec, float constant);
struct Vector vecDiv(struct Vector left, struct Vector right);
int vecEqual(struct Vector left, struct Vector right);
int vecMoreThanConst(struct Vector v, float f);
float vecMagnitude(struct Vector input);
struct Vector getHighestCorrelation(struct Vector left, struct Vector right);
#endif