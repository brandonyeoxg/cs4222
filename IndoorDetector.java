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
    private int lastKnownTimeStamp;
    private ArrayList<ActivityData> lightSamples, humiditySamples, temperatureSamples;
    private int lightSamplesStartIndex, lightSamplesEndIndex;
    private int humiditySamplesStartIndex, humiditySamplesEndIndex;
    private int temperatureSamplesStartIndex, temperatureSamplesEndIndex;
    private int lightTotalSampleSize, humidityTotalSampleSize, temperatureTotalSampleSize;
    
    public IndoorDetector() {
        state = ActivityState.INDOOR;
        lastKnownState = state;
        lastKnownTimeStamp = 0;

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
        addToLightSamples(lightList);
        addToHumiditySamples(humidList);
        addToTempSamples(tempList);

        if (lightTotalSampleSize <= 1 && humidityTotalSampleSize <= 1 && temperatureTotalSampleSize <= 1) { 
        // at the start of sampling, not enough data for S.D
        return estimateStateWithoutStandardDev(temperatureSamples, lightSamples, humiditySamples);    
        } else {
            lastKnownTimeStamp = getLatestTimeStamp();
            float standardDevForTemp = calculateStandardDev(temperatureSamples, temperatureSamplesStartIndex, temperatureSamplesEndIndex);
            float standardDevForLight = calculateStandardDev(lightSamples, lightSamplesStartIndex, lightSamplesEndIndex);
            float standardDevForHumidity = calculateStandardDev(humiditySamples, humiditySamplesStartIndex, humiditySamplesEndIndex);

            boolean isBeyondTempStandDev = hasExceedStandardDevThreshold(TEMPERATURE_STANDARDDEV_THRESHOLD, standardDevForTemp);
            boolean isBeyondLightStandDev = hasExceedStandardDevThreshold(LIGHT_STANDARDDEV_THRESHOLD, standardDevForLight);
            boolean isBeyondHumidityStandDev = hasExceedStandardDevThreshold(HUMID_STANDARDDEV_THRESHOLD, standardDevForHumidity);

            if (isBeyondTempStandDev || isBeyondLightStandDev || isBeyondHumidityStandDev) {
                changeState();
                return new OutputState(lastKnownTimeStamp, lastKnownState);
            } else {
                return new OutputState(lastKnownTimeStamp, lastKnownState);
            }
        }


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

    private void addToLightSamples(ArrayList<ActivityData> lightList) {
        for (int i = 0; i < lightList.size(); i++) {
            this.lightSamples.add(lightList.get(i));
            lightTotalSampleSize++;
            if (lightTotalSampleSize > WINDOW_SIZE) {
                lightSamplesStartIndex++;
                lightSamplesEndIndex++;
            } else {
                lightSamplesEndIndex++;
            }
        }
    }

    private void addToHumiditySamples(ArrayList<ActivityData> humidityList) {
        for (int i = 0; i < humidityList.size(); i++) {
            this.humiditySamples.add(humidityList.get(i));
            humidityTotalSampleSize++;
            if (humidityTotalSampleSize > WINDOW_SIZE) {
                humiditySamplesStartIndex++;
                humiditySamplesEndIndex++;
            } else {
                humiditySamplesEndIndex++;
            }
        }
    }

    private void addToTempSamples(ArrayList<ActivityData> tempList) {
        for (int i = 0; i < tempList.size(); i++) {
            this.temperatureSamples.add(tempList.get(i));
            temperatureTotalSampleSize++;
            if (temperatureTotalSampleSize > WINDOW_SIZE) {
                temperatureSamplesStartIndex++;
                temperatureSamplesEndIndex++;
            } else {
                temperatureSamplesEndIndex++;
            }
        }
    }

    private boolean hasExceedStandardDevThreshold(float threshold, float value) {
        if (value >= threshold) {
            int dataTimestamp = 0;
            if (threshold == TEMPERATURE_STANDARDDEV_THRESHOLD) {
                dataTimestamp = temperatureSamples.get(temperatureSamplesEndIndex - 1).data.get(0);
            } if (threshold == LIGHT_STANDARDDEV_THRESHOLD) {
                dataTimestamp = lightSamples.get(lightSamplesEndIndex - 1).data.get(0);
            } if (threshold == HUMID_STANDARDDEV_THRESHOLD) {
                dataTimestamp = humiditySamples.get(humiditySamplesEndIndex - 1).data.get(0);
            } if (dataTimestamp < this.lastKnownTimeStamp) {
                this.lastKnownTimeStamp = dataTimestamp;
            }
            return true;
        } else {
            return false;
        }
    }

    private void changeState() {
        if (this.lastKnownState == ActivityState.INDOOR) {
            this.lastKnownState =  ActivityState.OUTDOOR;
            this.state = this.lastKnownState;
        } else {
            this.lastKnownState = ActivityState.INDOOR;
        }
    }

    private int getLatestTimeStamp() {
        int timestamp = 0;
        if (humiditySamples.size() != 0) {
           int humidityTimeStamp = humiditySamples.get(humiditySamplesEndIndex - 1).timestamp;
           if (humidityTimeStamp > timestamp) {
            timestamp = humidityTimeStamp;
           }
        } if (lightSamples.size() != 0) {
           int lightTimeStamp = lightSamples.get(lightSamplesEndIndex - 1).timestamp;
           if (lightTimeStamp > timestamp) {
            timestamp = lightTimeStamp;
           }
        } if (temperatureSamples.size() != 0) {
           int temperatureTimeStamp = temperatureSamples.get(temperatureSamplesEndIndex - 1).timestamp;
           if (temperatureTimeStamp > timestamp) {
            timestamp = temperatureTimeStamp;
           }
        }
        return timestamp;
    }

    private OutputState estimateStateWithoutStandardDev(ArrayList<ActivityData> tempList, ArrayList<ActivityData> lightList, ArrayList<ActivityData> humidList) { 
    // to be implemented based on earlier no window implementation
        if (lightList.size() != 0) {
            ActivityData lightData = lightList.get(0);
            float lightCurrent = lightData.data.get(0);
            if ((lightCurrent > OUTDOOR_LIGHT_THRESHOLD)) {
                OutputState tentative = new OutputState(lightData.timestamp, ActivityState.OUTDOOR);
                return tentative;
            }
        } else {
            return new OutputState(lightData.timestamp, ActivityState.INDOOR);
        }
    }
}


