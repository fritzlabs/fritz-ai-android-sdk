package ai.fritz.visionCV.filter;

import org.opencv.core.Point;

import ai.fritz.vision.filter.OneEuroFilter;

public class OneEuroPointFilter {

    private OneEuroFilter[] filters;
    private long startTime = 0;

    public OneEuroPointFilter(double minCutoff, double beta, double derivateCutoff) {
        filters = new OneEuroFilter[2];
        try {
            filters[0] = new OneEuroFilter(
                    minCutoff,
                    beta,
                    derivateCutoff
            );

            filters[1] = new OneEuroFilter(
                    minCutoff,
                    beta,
                    derivateCutoff
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Point filter(Point point) {
        OneEuroFilter filterX = filters[0];
        OneEuroFilter filterY = filters[1];

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        double timestamp = ((double) (System.currentTimeMillis() - startTime)) / 1000;

        try {
            double resultX = filterX.filter(point.x, timestamp);
            double resultY = filterY.filter(point.y, timestamp);

            return new Point(resultX, resultY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
