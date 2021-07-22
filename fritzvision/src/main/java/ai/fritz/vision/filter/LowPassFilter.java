package ai.fritz.vision.filter;

/**
 * https://github.com/SableRaf/signalfilter/blob/master/src/LowPassFilter.java
 *
 * @author s. conversy from n. roussel c++ version
 */

class LowPassFilter {

    double y, a, s;
    boolean initialized;

    void setAlpha(double alpha) {
        if (alpha <= 0.0) {
            a = 0;
        }
        if (alpha > 1.0) {
            a = 1;
        }
        a = alpha;
    }

    public LowPassFilter(double alpha) {
        init(alpha, 0);
    }

    public LowPassFilter(double alpha, double initval) {
        init(alpha, initval);
    }

    private void init(double alpha, double initval) {
        y = s = initval;
        setAlpha(alpha);
        initialized = false;
    }

    public double filter(double value) {
        double result;
        if (initialized) {
            result = a * value + (1.0 - a) * s;
        } else {
            result = value;
            initialized = true;
        }
        y = value;
        s = Double.isNaN(result) ? value : result;
        return result;
    }

    public double filterWithAlpha(double value, double alpha) {
        setAlpha(alpha);
        return filter(value);
    }

    public boolean hasLastRawValue() {
        return initialized;
    }

    public double lastRawValue() {
        return y;
    }
}
