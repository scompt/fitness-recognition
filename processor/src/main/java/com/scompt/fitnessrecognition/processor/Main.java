package com.scompt.fitnessrecognition.processor;

import com.scompt.fitnessrecognition.common.PeakDetProcessor;
import com.scompt.fitnessrecognition.common.Processor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final int CHUNK_SIZE = 10;
    public static final int DELTA = 20;

    public static void main(String[] args) throws IOException {
        Processor processor = new PeakDetProcessor(DELTA);

        List<Float> maxes = new ArrayList<>();
        List<Long> maxTimestamps = new ArrayList<>();

        try (ObjectInputStream objectInputStream = new ObjectInputStream(System.in)) {
            while (objectInputStream.available() > 0) {
                int chunkType = objectInputStream.readInt();

                if (chunkType != 0) {
                    throw new IllegalStateException("Unknown chunk type: " + chunkType);
                }

                for (int i = 0; i < CHUNK_SIZE; i++) {
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

        System.out.println();
        for (int i = 0; i < maxes.size(); i++) {
            Long timestamp = maxTimestamps.get(i);
            Float value = maxes.get(i);

            System.out.format("%d, %f\n", timestamp, value);
        }
    }
}
