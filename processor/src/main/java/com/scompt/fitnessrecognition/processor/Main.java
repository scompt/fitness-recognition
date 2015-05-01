package com.scompt.fitnessrecognition.processor;

import com.scompt.fitnessrecognition.common.Constants;
import com.scompt.fitnessrecognition.common.PeakDetProcessor;
import com.scompt.fitnessrecognition.common.Processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class Main {

    public static final int DELTA = 20;

    private long mMinimumTimestamp = -1;

    public static void main(String[] args) throws IOException {
        Processor processor = new PeakDetProcessor(DELTA);
//        Processor processor = new SystemOutProcessor();

        new Main().processStream(System.in, processor);
    }

    public void processStream(InputStream stream, Processor processor) throws IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(stream)) {
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
    }

    private void processBatteryChunk(ObjectInputStream objectInputStream) throws IOException {
        float batteryPercentage = objectInputStream.readFloat();
        System.err.println("Battery percentage: " + batteryPercentage);
    }

    private void processDataChunk(Processor processor, ObjectInputStream objectInputStream) throws IOException {
        for (int i = 0; i < Constants.CHUNK_SIZE; i++) {
            long timestamp = objectInputStream.readLong();
            if (mMinimumTimestamp < 0) {
                mMinimumTimestamp = timestamp;
            }

            float value1 = objectInputStream.readFloat();
            float value2 = objectInputStream.readFloat();
            float value3 = objectInputStream.readFloat();
            float magnitude = (float) Math.sqrt(value1 * value1 + value2 * value2 + value3 * value3);

            boolean signal = processor.processPoint(timestamp, value1, value2, value3);

            System.out.format("%d, %f, %d\n", timestamp - mMinimumTimestamp, magnitude, signal ? 1 : 0);
        }
    }
}
