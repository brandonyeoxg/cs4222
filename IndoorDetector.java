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
    
    public IndoorDetector() {
        System.out.println("Hello IndoorDetector here");
    }
    
    public OutputState compute(ArrayList<ActivityData> tempList, ArrayList<ActivityData> lightList, ArrayList<ActivityData> humidList) {
        // Confidence level stores how confident are you INDOOR. For how confident are you outdoor, use "1-"
        float tempConfidence = 0.0f;
        float lightConfidence = 0.0f;
        float humidConfidence = 0.0f;
        float totalConfidence = 0.0f;

        // Dynamically set weighting in case of packet lost. - Need to refine
//        if (tempList.isEmpty()) {
//            LIGHT_WEIGHTING = 0.8f;
//            TEMP_WEIGHTING = 0.0f;
//        } else if (humidList.isEmpty()) {
//            HUMID_WEIGHTING = 0.0f;
//            LIGHT_WEIGHTING = 0.8f;
//        } else if (humidList.isEmpty() && tempList.isEmpty()) {
//            LIGHT_WEIGHTING = 1.0f;
//            TEMP_WEIGHTING = 0.0f;
//            HUMID_WEIGHTING = 0.0f;
//        }
//
        ArrayList<OutputState> confirmedResults = new ArrayList<OutputState>();

        ArrayList<Float> predictIndoor = new ArrayList<Float>();
        
        for (int i = 0; i < lightList.size(); i++) {
            ActivityData lightData = lightList.get(i);
            float lightCurrent = lightData.data.get(0) * 100;
            if ((lightCurrent > OUTDOOR_LIGHT_THRESHOLD)) {
                OutputState tentative = new OutputState(lightData.timestamp, ActivityState.OUTDOOR);
//                confirmedResults.add(tentative);
                return tentative;
            } else if ((lightCurrent <= OUTDOOR_LIGHT_THRESHOLD) && (lightCurrent > INDOOR_LIGHT_THRESHOLD)) {
                lightConfidence = computeConfidenceLevel(lightCurrent, OUTDOOR_LIGHT_THRESHOLD);
//                System.out.println(lightConfidence);
            } else if (lightCurrent <= INDOOR_LIGHT_THRESHOLD) {
                lightConfidence = 1 - computeConfidenceLevel(lightCurrent, INDOOR_LIGHT_THRESHOLD);
//                System.out.println(lightConfidence);
            }
//            if (confirmedResults.size() == 2) {
//                OutputState firstTemp = confirmedResults.get(0);
//                OutputState secondTemp = confirmedResults.get(1);
//                if (firstTemp.activityState == secondTemp.activityState) {
//                    return firstTemp;
//                }
//            } else if ((confirmedResults.size() == 1) && (lightList.size() == 1)) {
//                return confirmedResults.get(0);
//            }
        }
        predictIndoor.add(lightConfidence * LIGHT_WEIGHTING);
        

        for (int j = 0; j < tempList.size(); j++) {
            ActivityData tempData = tempList.get(j);
            float tempCurrent = tempData.data.get(0);
            if (tempCurrent <= TEMPERATURE_THRESHOLD) {
                tempConfidence = computeConfidenceLevel(tempCurrent, TEMPERATURE_THRESHOLD);
                predictIndoor.add(tempConfidence * TEMP_WEIGHTING);
//                System.out.println(tempConfidence);
            } else {
                tempConfidence = 1 - computeConfidenceLevel(tempCurrent, TEMPERATURE_THRESHOLD);
                predictIndoor.add(tempConfidence * TEMP_WEIGHTING);
//                System.out.println(tempConfidence);
            }
        }
        
        for (int k = 0; k < humidList.size(); k++) {
            ActivityData humidData = humidList.get(k);
            float humidCurrent = humidData.data.get(0);
            if (humidCurrent <= HUMID_THRESHOLD) {
                humidConfidence = computeConfidenceLevel(humidCurrent, HUMID_THRESHOLD);
                predictIndoor.add(humidConfidence * HUMID_WEIGHTING);
//                System.out.println(humidConfidence);
            } else {
                humidConfidence = 1 - computeConfidenceLevel(humidCurrent, HUMID_THRESHOLD);
                predictIndoor.add(humidConfidence * HUMID_WEIGHTING);
//                System.out.println("--" + humidConfidence);
            }
        }
        
//        System.out.println("Confidence Level: L - " + (lightConfidence*LIGHT_WEIGHTING) +
//                           " T - " + (tempConfidence*TEMP_WEIGHTING) + " H - " +
//                           (humidConfidence*HUMID_WEIGHTING));
        // totalConfidence is how confident are you in saying that it is in indoor.
        totalConfidence = getSumOfArrayList(predictIndoor);
//        System.out.println("TOTAL CONFIDENCE: " + totalConfidence + "\n");
        int timestamp = getMeanTimeStamp(tempList, lightList, humidList);
//        System.out.println("Time Stamp is: " + timestamp);
        if (timestamp != -1) {
            if (totalConfidence > CONFIDENCE_LEVEL_THRESHOLD){
//                System.out.println("TOTAL CONFIDENCE: " + totalConfidence + "\n");
                System.out.println("\n");
                return new OutputState(timestamp, ActivityState.INDOOR);
            } else if (totalConfidence < 1 - CONFIDENCE_LEVEL_THRESHOLD) {
                System.out.println("TOTAL CONFIDENCE: " + totalConfidence);
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
