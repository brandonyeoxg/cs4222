import java.util.ArrayList;
import java.lang.Math;

public class IndoorDetector {
    public static final float CONFIDENCE_LEVEL_THRESHOLD = 0.6f;
    public static final float INDOOR_LIGHT_THRESHOLD = 50.0f;
    public static final float OUTDOOR_LIGHT_THRESHOLD = 2000.0f;
    public static final float HUMID_THRESHOLD = 70.0f;
    public static final float TEMPERATURE_THRESHOLD = 24.0f;
    public static float LIGHT_WEIGHTING = 0.60f;
    public static float TEMP_WEIGHTING = 0.2f;
    public static float HUMID_WEIGHTING = 0.2f;

    private final int WINDOW_SIZE = 3;
    private final float LIGHT_STANDARDDEV_THRESHOLD = 150.0f;
    private final float HUMID_STANDARDDEV_THRESHOLD = 10.0f;
    private final float TEMPERATURE_STANDARDDEV_THRESHOLD = 2.0f;

    private String state, lastKnownState;
    private ArrayList<ActivityData> lightSamples, humiditySamples, temperatureSamples;
    private int lightSamplesStartIndex, lightSamplesEndIndex;
    private int humiditySamplesStartIndex, humiditySamplesEndIndex;
    private int temperatureSamplesStartIndex, temperatureSamplesEndIndex;
    private int lightTotalSampleSize, humidityTotalSampleSize, temperatureTotalSampleSize;
    
    public IndoorDetector() {
        state = ActivityState.IDLE;
        lastKnownState = state;

        lightSamples = new ArrayList<ActivityData>;
        humiditySamples = new ArrayList<ActivityData>;
        temperatureSamples = new ArrayList<ActivityData>;

        lightSamplesStartIndex = 0;
        lightSamplesEndIndex = 0;
        humiditySamplesStartIndex = 0;
        humiditySamplesEndIndex = 0;
        temperatureSamplesStartIndex = 0;
        temperatureSamplesEndIndex = 0;

        lightTotalSampleSize = 0;
        humidityTotalSampleSize = 0;
        temperatureTotalSampleSize = 0;

        System.out.println("Hello IndoorDetector here");
    }
    
    public OutputState compute(ArrayList<ActivityData> tempList, ArrayList<ActivityData> lightList, ArrayList<ActivityData> humidList) {
    }

    private float calculateStandardDev(ArrayList<ActivityData> dataList, int startIndex, int endIndex) {
        int numOfData = endIndex - startIndex;
        float meanOfData = calculateMean(dataList, startIndex, endIndex);
        float summationOfDataMinusMeanSquared = 0.0f;
        for (int i = startIndex; i < endIndex; i++) {
            float currentData = dataList.get(i).data.get(0);
            float dataMinusMeanSquared = (currentData - meanOfData) * (currentData - meanOfData);
            summationOfDataMinusMeanSquared += dataMinusMeanSquared;
        }
        float standardDev = (float)Math.sqrt(summationOfDataMinusMeanSquared / numOfData);
        return standardDev;
    }

    private float calculateMean(ArrayList<ActivityData> dataList, int startIndex, int endIndex) {
        int numOfData = endIndex - startIndex;
        float sumOfAllData = 0.0f;
        for (int i = startIndex; i < endIndex; i++) {
            float currentData = dataList.get(i).data.get(0);
            sumOfAllData += currentData;
        }
        float meanOfAllData = sumOfAllData / numOfData;
        return meanOfAllData;
    }
}


