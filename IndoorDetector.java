import java.util.ArrayList;

public class IndoorDetector {
    public static final float CONFIDENCE_LEVEL_THRESHOLD = 0.50f;
    public static final float INDOOR_LIGHT_THRESHOLD = 70.0f;
    public static final float OUTDOOR_LIGHT_THRESHOLD = 2000.0f;
    public static final float HUMID_THRESHOLD = 70.0f;
    public static final float TEMPERATURE_THRESHOLD = 24.0f;
    public static final float LIGHT_WEIGHTING = 0.6f;
    public static final float TEMP_WEIGHTING = 0.2f;
    public static final float HUMID_WEIGHTING = 0.2f;
    
    public IndoorDetector() {
        System.out.println("Hello IndoorDetector here");
    }
    
    public OutputState compute(ArrayList<ActivityData> tempList, ArrayList<ActivityData> lightList, ArrayList<ActivityData> humidList) {
    // Using weighted sum to calculate confidence level (lightData = 0.6, tempData = 0.2, humidData = 0.2)
        float tempConfidence;
        float lightConfidence;
        float humidConfidence;
        float totalConfidence;

        ArrayList<OutputState> confirmedResults = new ArrayList<OutputState>();
        
        // Closer to 1 means indoor.
        ArrayList<Float> predictIndoor = new ArrayList<Float>();
        
        for (int i = 0; i < lightList.size(); i++) {
            ActivityData lightData = lightList.get(i);
            float lightCurrent = lightData.data.get(0);
            if ((lightCurrent > OUTDOOR_LIGHT_THRESHOLD) || (lightCurrent < INDOOR_LIGHT_THRESHOLD)) {
                OutputState tentative = new OutputState(lightData.timestamp, ActivityState.OUTDOOR);
                confirmedResults.add(tentative);
            } else {
                lightConfidence = computeConfidenceLevel(lightCurrent, OUTDOOR_LIGHT_THRESHOLD);
            }
            if (confirmedResults.size() == 2) {
                OutputState firstTemp = confirmedResults.get(0);
                OutputState secondTemp = confirmedResults.get(1);
                if (firstTemp.activityState == secondTemp.activityState) {
                    return firstTemp;
                }
            } else if ((confirmedResults.size() == 1) && (lightList.size() == 1)) {
                return confirmedResults.get(0);
            }
        }
        predictIndoor.add(lightConfidence * LIGHT_WEIGHTING);
        
        for (int j = 0; j < tempList.size(); j++) {
            ActivityData tempData = tempList.get(j);
            float tempCurrent = tempData.data.get(0);
            if (tempCurrent <= TEMPERATURE_THRESHOLD) {
                tempConfidence = computeConfidenceLevel(tempCurrent, TEMPERATURE_THRESHOLD);
                predictIndoor.add(tempConfidence * TEMP_WEIGHTING);
                break;
            }
        }
        
        for (int k = 0; k < humidList.size(); k++) {
            ActivityData humidData = humidList.get(k);
            float humidCurrent = humidData.data.get(0);
            if (humidCurrent <= HUMID_THRESHOLD) {
                humidConfidence = computeConfidenceLevel(humidCurrent, HUMID_THRESHOLD);
                predictIndoor.add(humidConfidence * HUMID_WEIGHTING);
                break;
            }
        }
        
        // totalConfidence is how confident are you in saying that it is in indoor.
        totalConfidence = getSumOfArrayList(predictIndoor);
        int timestamp = getMeanTimeStamp(tempList, lightList, humidList);
        if (timestamp != -1) {
            if (totalConfidence > CONFIDENCE_LEVEL_THRESHOLD) {
                return new OutputState(timestamp, ActivityState.INDOOR);
            } else {
                return new OutputState(timestamp, ActivityState.OUTDOOR);
            }
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
                answer += tempList.get(i).data.get(0);
            }
            for (int j = 0; j < lightList.size(); j++) {
                answer += lightList.get(j).data.get(0);
            }
            for (int k = 0; k < humidList.size(); k++) {
                answer += humidList.get(k).data.get(0);
            }
            return Math.round(answer / totalNumberOfElements);
        } else {
            return -1;
        }
    }
}
