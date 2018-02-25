#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_BUF 255

struct Vector {
	float x;
	float y;
	float z;
};

int getCSVLineCount(FILE *file);
void getAccelInfo(FILE *file, struct Vector* dataSets, int numLines);
struct Vector* getAccelData(char *filename);
void printDataSets(struct Vector* dataSets, int numLines);

int main(int argc, char* argv[]) {
	if (argc < 2) {
		printf("Usage of this program: ./count_step team_accel.csv\n");
		return -1;
	}
	struct Vector *dataSets = getAccelData(argv[1]);

	return 0;
}

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

void getAccelInfo(FILE *file, struct Vector* dataSets, int numLines) {
	printf("Getting Accel info\n");
	char buf[MAX_BUF] = {0x0};
	int i = 0;
	char* field;
	while(i < numLines) {
		fgets(buf, MAX_BUF, (FILE*)file);
		field = strtok(buf, ",");
		dataSets[i].x = atof(field);

		field = strtok(NULL, ",");
		dataSets[i].y = atof(field);

		field = strtok(NULL, ",");
		dataSets[i].z = atof(field);
		
		i += 1;
	}
}

struct Vector* getAccelData(char *filename) {
	printf("Opening file: %s\n", filename);
	FILE *file = fopen(filename, "r'");
	if (file == NULL) {
		printf("File does not exist!!\n");
		exit(EXIT_FAILURE);
	}

	int numLines = getCSVLineCount(file);
	
	printf("File has: %d lines\n", numLines);
	struct Vector* dataSets = (struct Vector*) malloc(sizeof(struct Vector)*numLines);

	fseek(file, 0, SEEK_SET);
	getAccelInfo(file, dataSets, numLines);

	printDataSets(dataSets, numLines);
	return dataSets;
}

void printDataSets(struct Vector* dataSets, int numLines) {
	int i;

	for (i = 0; i < numLines; i++ ) {
		printf("Data %i: X=%.2f, Y=%.2f, Z=%.2f\n", i + 1, dataSets[i].x, dataSets[i].y, dataSets[i].z);
	}
}