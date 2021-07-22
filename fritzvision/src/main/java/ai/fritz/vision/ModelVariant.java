package ai.fritz.vision;

/**
 * Running models on-device involves working with different resource constraints than running
 * models in the cloud.
 *
 * To help with this, we have created multiple model variants for developers to choose from
 * that best fits their use case.
 */
public enum ModelVariant {

    /**
     * Optimized for speed. Fast models are best for use in applications
     * that require processing on video streams in real-time or on older devices.
     * As a result, models optimized for speed may be lower resolution than the accurate models.
     */
    FAST,

    /**
     * Optimized for size. Small models keep your application bundle size low and conserve bandwidth
     * when downloaded over-the-air. They generally will be slightly less accurate than
     * their larger counterparts.
     */
    SMALL,

    /**
     * Optimized for higher accuracy. Accurate models are great for applications running
     * on still images or background video processing where prediction quality is
     * more important than speed.
     */
    ACCURATE
}
