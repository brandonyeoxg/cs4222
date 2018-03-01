#include "Vector.h"

void vecInit(struct Vector *inVec) {
	inVec->x = 0.0;
	inVec->y = 0.0;
	inVec->z = 0.0;
}

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