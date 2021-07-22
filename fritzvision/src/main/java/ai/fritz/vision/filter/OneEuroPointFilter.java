package ai.fritz.vision.filter;

import android.graphics.PointF;

public class OneEuroPointFilter {

    private OneEuroFilter[] keypointFilters;
    private long startTime = 0;

    public OneEuroPointFilter(double minCutoff, double beta, double derivateCutoff) {
        keypointFilters = new OneEuroFilter[2];
        keypointFilters[0] = new OneEuroFilter(minCutoff, beta, derivateCutoff);
        keypointFilters[1] = new OneEuroFilter(minCutoff, beta, derivateCutoff);
    }

    public PointF filter(PointF point) {

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        double timestamp = ((double) (System.currentTimeMillis() - startTime)) / 1000;

        double smoothedX = keypointFilters[0].filter(point.x, timestamp);
        double smoothedY = keypointFilters[1].filter(point.y, timestamp);

        return new PointF((float) smoothedX, (float) smoothedY);
    }
}

