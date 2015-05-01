package com.scompt.fitnessrecognition.common;

public interface Processor {
    boolean processPoint(long timestamp, float value1, float value2, float value3);
}

