import java.util.ArrayList;

public class IndoorDetector {
    public static final float CONFIDENCE_LEVEL_THRESHOLD = 0.6f;
    public static final float INDOOR_LIGHT_THRESHOLD = 50.0f;
    public static final float OUTDOOR_LIGHT_THRESHOLD = 2000.0f;
    public static final float HUMID_THRESHOLD = 70.0f;
    public static final float TEMPERATURE_THRESHOLD = 24.0f;
    public static float LIGHT_WEIGHTING = 0.60f;
    public static float TEMP_WEIGHTING = 0.2f;
    public static float HUMID_WEIGHTING = 0.2f;
    public static final int WINDOW_SIZE = 3;
    
    public IndoorDetector() {
        System.out.println("Hello IndoorDetector here");
    }
    
    public OutputState compute(ArrayList<ActivityData> tempList, ArrayList<ActivityData> lightList, ArrayList<ActivityData> humidList) {
        float tempConfidence = 0.0f;
        float lightConfidence = 0.0f;
        float humidConfidence = 0.0f;
        float totalConfidence = 0.0f;
        
        ArrayList<OutputState> confirmedResults = new ArrayList<OutputState>();
        
        ArrayList<Float> predictIndoor = new ArrayList<Float>();
        
        for (int i = 0; i < lightList.size(); i++) {
            ActivityData lightData = lightList.get(i);
            float lightCurrent = lightData.data.get(0);
            if ((lightCurrent > OUTDOOR_LIGHT_THRESHOLD)) {
                OutputState tentative = new OutputState(lightData.timestamp, ActivityState.OUTDOOR);
                return tentative;
            } else if ((lightCurrent <= OUTDOOR_LIGHT_THRESHOLD) && (lightCurrent > INDOOR_LIGHT_THRESHOLD)) {
                lightConfidence = computeConfidenceLevel(lightCurrent, OUTDOOR_LIGHT_THRESHOLD);
            } else if (lightCurrent <= INDOOR_LIGHT_THRESHOLD) {
                lightConfidence = 1 - computeConfidenceLevel(lightCurrent, INDOOR_LIGHT_THRESHOLD);
            }
        }
        predictIndoor.add(lightConfidence * LIGHT_WEIGHTING);
        
        
        for (int j = 0; j < tempList.size(); j++) {
            ActivityData tempData = tempList.get(j);
            float tempCurrent = tempData.data.get(0);
            if (tempCurrent <= TEMPERATURE_THRESHOLD) {
                tempConfidence = 1;
                return new OutputState(tempData.timestamp, ActivityState.INDOOR);
            } else {
                tempConfidence = 1 - computeConfidenceLevel(tempCurrent, TEMPERATURE_THRESHOLD);
                predictIndoor.add(tempConfidence * TEMP_WEIGHTING);
            }
        }
        
        for (int k = 0; k < humidList.size(); k++) {
            ActivityData humidData = humidList.get(k);
            float humidCurrent = humidData.data.get(0);
            if (humidCurrent <= HUMID_THRESHOLD) {
                humidConfidence = computeConfidenceLevel(humidCurrent, HUMID_THRESHOLD);
                predictIndoor.add(humidConfidence * HUMID_WEIGHTING);
            } else {
                humidConfidence = 1 - computeConfidenceLevel(humidCurrent, HUMID_THRESHOLD);
                predictIndoor.add(humidConfidence * HUMID_WEIGHTING);
            }
        }
        
        totalConfidence = getSumOfArrayList(predictIndoor);
        int timestamp = getMeanTimeStamp(tempList, lightList, humidList);
        if (timestamp != -1) {
            if (totalConfidence > CONFIDENCE_LEVEL_THRESHOLD){
                return new OutputState(timestamp, ActivityState.INDOOR);
            } else if ((1 - totalConfidence) > CONFIDENCE_LEVEL_THRESHOLD) {
                return new OutputState(timestamp, ActivityState.OUTDOOR);
            } else {
                return new OutputState(-1, ActivityState.INDOOR);
            }
        } else {
            return new OutputState(-1, ActivityState.OUTDOOR);
        }
    }
    private float computeConfidenceLevel(float reading, float threshold) {
        return Math.abs((threshold - reading) / threshold);
    }
    private float getSumOfArrayList(ArrayList<Float> list) {
        float sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }
    private int getMeanTimeStamp(ArrayList<ActivityData> tempList, ArrayList<ActivityData> lightList, ArrayList<ActivityData> humidList) {
        int totalNumberOfElements = tempList.size() + lightList.size() + humidList.size();
        int answer = 0;
        if (totalNumberOfElements != 0) {
            for (int i = 0; i < tempList.size(); i++) {
                answer += tempList.get(i).timestamp;
            }
            for (int j = 0; j < lightList.size(); j++) {
                answer += lightList.get(j).timestamp;
            }
            for (int k = 0; k < humidList.size(); k++) {
                answer += humidList.get(k).timestamp;
            }
            return Math.round(answer / totalNumberOfElements);
        } else {
            return -1;
        }
    }
}


