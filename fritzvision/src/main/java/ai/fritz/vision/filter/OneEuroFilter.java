package ai.fritz.vision.filter;

/**
 * https://raw.githubusercontent.com/SableRaf/signalfilter/master/src/OneEuroFilter.java
 *
 * @author s. conversy from n. roussel c++ version
 */
public class OneEuroFilter {

    double freq;
    double mincutoff;
    double beta_;
    double dcutoff;
    LowPassFilter x;
    LowPassFilter dx;
    double lasttime;
    double UndefinedTime = -1;

    double alpha(double cutoff) {
        double te = 1.0 / freq;
        double tau = 1.0 / (2 * Math.PI * cutoff);
        return 1.0 / (1.0 + tau / te);
    }


    // SETTERS ---------------------------------------------


    /**
     * @param f Frequency parameter of the OneEuro Filter
     */
    public void setFrequency(double f) {
        if (f <= 0) {
            throw new RuntimeException("freq should be >0");
        }
        freq = f;
    }

    /**
     * @param mc Minimum cutoff (intercept) parameter of the OneEuro Filter
     */
    public void setMinCutoff(double mc) {
        if (mc <= 0) {
            throw new RuntimeException("mincutoff should be >0");
        }
        mincutoff = mc;
    }

    /**
     * @param b Beta (cutoff slope) parameter of the OneEuro Filter
     */
    public void setBeta(double b) {
        beta_ = b;
    }

    /**
     * @param dc Cutoff for derivative parameter of the OneEuro Filter
     */
    public void setDerivateCutoff(double dc) {
        if (dc <= 0) {
            throw new RuntimeException("dcutoff should be >0");
        }
        dcutoff = dc;
    }

    // ---------------------------------------------------------


    // CONSTRUCTORS ---------------------------------------------------------

    public OneEuroFilter() {
        init(1.0, 0.0, 1.0);
    }

    public OneEuroFilter(double mincutoff) {
        init(mincutoff, 0.0, 1.0);
    }

    public OneEuroFilter(double mincutoff, double beta_) {
        init(mincutoff, beta_, 1.0);
    }

    public OneEuroFilter(double mincutoff, double beta_, double dcutoff) {
        init(mincutoff, beta_, dcutoff);
    }

    // --------------------------------------------------------------------------


    private void init(double mincutoff, double beta_, double dcutoff) {
        setMinCutoff(mincutoff);
        setBeta(beta_);
        setDerivateCutoff(dcutoff);
        x = new LowPassFilter(alpha(mincutoff));
        dx = new LowPassFilter(alpha(dcutoff));
        lasttime = UndefinedTime;
    }

    public double filter(double value) {
        return filter(value, UndefinedTime);
    }

    public double filter(double value, double timestamp) {
        // update the sampling frequency based on timestamps
        if (lasttime != UndefinedTime && timestamp != UndefinedTime && (timestamp - lasttime) > 0) {
            freq = 1.0 / (timestamp - lasttime);
        } else {
            freq = 1;
        }

        lasttime = timestamp;
        // estimate the current variation per second
        double dvalue = x.hasLastRawValue() ? (value - x.lastRawValue()) * freq : 0.0;
        double edvalue = dx.filterWithAlpha(dvalue, alpha(dcutoff));
        // use it to update the cutoff frequency
        double cutoff = mincutoff + beta_ * Math.abs(edvalue);
        // filter the given value
        return x.filterWithAlpha(value, alpha(cutoff));
    }
}
