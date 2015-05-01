package com.scompt.fitnessrecognition.processor;

import com.scompt.fitnessrecognition.common.Constants;
import com.scompt.fitnessrecognition.common.PeakDetProcessor;
import com.scompt.fitnessrecognition.common.Processor;
import com.scompt.fitnessrecognition.common.SystemOutProcessor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final int DELTA = 20;

    private static List<Float> maxes = new ArrayList<>();
    private static List<Long> maxTimestamps = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Processor processor = new PeakDetProcessor(DELTA);
//        Processor processor = new SystemOutProcessor();

        try (ObjectInputStream objectInputStream = new ObjectInputStream(System.in)) {
            while (objectInputStream.available() > 0) {
                int chunkType = objectInputStream.readInt();

                switch (chunkType) {
                    case Constants.BATTERY_CHUNK_TYPE:
                        processBatteryChunk(objectInputStream);
                        break;

                    case Constants.DATA_CHUNK_TYPE:
                        processDataChunk(processor, objectInputStream);
                        break;

                    default:
                        throw new IllegalStateException("Unknown chunk type: " + chunkType);
                }
            }
        }

        System.out.println();
        for (int i = 0; i < maxes.size(); i++) {
            Long timestamp = maxTimestamps.get(i);
            Float value = maxes.get(i);

            System.out.format("%d, %f\n", timestamp, value);
        }
    }

    private static void processBatteryChunk(ObjectInputStream objectInputStream) throws IOException {
        float batteryPercentage = objectInputStream.readFloat();
        System.err.println("Battery percentage: " + batteryPercentage);
    }

    private static void processDataChunk(Processor processor, ObjectInputStream objectInputStream) throws IOException {
        for (int i = 0; i < Constants.CHUNK_SIZE; i++) {
            long timestamp = objectInputStream.readLong();
            float value1 = objectInputStream.readFloat();
            float value2 = objectInputStream.readFloat();
            float value3 = objectInputStream.readFloat();

            boolean signal = processor.processPoint(timestamp, value1, value2, value3);

            if (signal) {
                float magnitude = (float) Math.sqrt(value1 * value1 + value2 * value2 + value3 * value3);
                maxTimestamps.add(timestamp);
                maxes.add(magnitude);
            }
        }
    }
}
