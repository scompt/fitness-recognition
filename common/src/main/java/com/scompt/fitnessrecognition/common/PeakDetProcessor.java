package com.scompt.fitnessrecognition.common;

// http://www.billauer.co.il/peakdet.html
public class PeakDetProcessor implements Processor {

    float minValue = Float.MIN_VALUE;
    float maxValue = Float.MAX_VALUE;
    long minTimestamp = 0;
    long maxTimestamp = 0;

    boolean lookForMax = true;
    private double delta;

    public PeakDetProcessor(double delta) {
        this.delta = delta;
    }

    @Override
    public boolean processPoint(long timestamp, float value1, float value2, float value3) {
        float magnitude = (float) Math.sqrt(value1 * value1 + value2 * value2 + value3 * value3);

        if (magnitude > maxValue) {
            maxValue = magnitude;
            maxTimestamp = timestamp;
        }
        if (magnitude < minValue) {
            minValue = magnitude;
            minTimestamp = timestamp;
        }

        if (lookForMax) {
            if (magnitude < maxValue - delta) {
                // record maxTimestamp and maxValue as a max point
                minValue = magnitude;
                minTimestamp = timestamp;
                lookForMax = false;

                // Only emit mins
                return false;
            }
        } else {
            if (magnitude > minValue + delta) {
                // record minTimestamp and minValue as a min point
                maxValue = magnitude;
                maxTimestamp = timestamp;
                lookForMax = true;
                return true;
            }
        }

        return false;
    }
}
