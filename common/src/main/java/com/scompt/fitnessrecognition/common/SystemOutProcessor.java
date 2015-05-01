package com.scompt.fitnessrecognition.common;

public class SystemOutProcessor implements Processor {
    @Override
    public boolean processPoint(long timestamp, float value1, float value2, float value3) {
        System.out.format("%d, %f, %f, %f\n", timestamp, value1, value2, value3);
        return false;
    }
}
