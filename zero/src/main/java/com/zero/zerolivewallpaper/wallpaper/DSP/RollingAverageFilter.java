package com.zero.zerolivewallpaper.wallpaper.DSP;

public class RollingAverageFilter {

    private final int size;
    private final int width;
    private final float[] total;
    private int index = 0;
    private final float[][] samples;

    public RollingAverageFilter(int width, int size) {
        this.size = size;
        this.width = width;

        samples = new float[size][width];
        total = new float[width];

        for (int i = 0; i < size; i++)
            for (int j = 0; j < width; j++)
                samples[i][j] = 0f;
    }

    public void add(float[] x) {
        for (int j = 0; j < width; j++)
            total[j] -= samples[index][j];

        samples[index] = x.clone();

        for (int j = 0; j < width; j++)
            total[j] += x[j];

        if (++index == size) index = 0;
    }

    public float[] getAverage() {
        float[] output = new float[width];

        for (int j = 0; j < width; j++)
            output[j] = total[j] / size;

        return output;
    }
}