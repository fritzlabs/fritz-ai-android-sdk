package ai.fritz.vision.filter;

/**
 * Uses the 1 Euro Filter to smooth the pose.
 * http://cristal.univ-lille.fr/~casiez/1euro/
 *
 * For an interactice demo of how to use the min cutoff, derivative cutoff, and beta to adjust the smoothing:
 * http://cristal.univ-lille.fr/~casiez/1euro/InteractiveDemo/
 */
public class OneEuroFilterMethod implements PoseSmoothingMethod {

    private static final double DEFAULT_DERIVATIVE_CUTOFF = .3;
    private static final double DEFAULT_MIN_CUTOFF = .2;
    private static final double DEFAULT_BETA = .01;

    private double minCutoff;
    private double beta;
    private double derivativeCutoff;

    public OneEuroFilterMethod() {
        this(DEFAULT_MIN_CUTOFF, DEFAULT_BETA, DEFAULT_DERIVATIVE_CUTOFF);
    }

    public OneEuroFilterMethod(double minCutoff, double beta, double derivativeCutoff) {
        this.minCutoff = minCutoff;
        this.beta = beta;
        this.derivativeCutoff = derivativeCutoff;
    }

    @Override
    public String getName() {
        return OneEuroFilterMethod.class.getSimpleName();
    }

    /**
     * Get the minimum frequency cutoff
     *
     * Lower values will decrease jitter but increase lag.
     *
     * @return the min cutoff.
     */
    public double getMinCutoff() {
        return minCutoff;
    }

    /**
     * Get the beta value
     *
     * Higher values of beta will help reduce lag, but may increase jitter.
     *
     * @return the beta.
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Get the derivative cutoff.
     *
     * Max derivative value allowed. Increasing will allow more sudden movements.
     *
     * @return the derivative cutoff.
     */
    public double getDerivativeCutoff() {
        return derivativeCutoff;
    }

    @Override
    public PoseSmoother buildPoseSmoother(int numParts) {
        return new EuroPoseSmoother(numParts,
                minCutoff,
                beta,
                derivativeCutoff);
    }
}
