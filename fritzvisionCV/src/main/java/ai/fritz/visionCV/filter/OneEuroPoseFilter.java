package ai.fritz.visionCV.filter;

import com.google.ar.core.Pose;

import org.opencv.core.Point;

import ai.fritz.vision.filter.OneEuroFilter;

public class OneEuroPoseFilter {

    private OneEuroFilter[] translationFilters;
    private OneEuroFilter[] rotationFilters;
    private long startTime = 0;

    public OneEuroPoseFilter(double minCutoff, double beta, double derivateCutoff) {
        translationFilters = new OneEuroFilter[3];
        rotationFilters = new OneEuroFilter[4];

        try {
            for (int i = 0; i < translationFilters.length; i++) {
                translationFilters[i] = new OneEuroFilter(
                        minCutoff,
                        beta,
                        derivateCutoff
                );
            }

            for (int i = 0; i < rotationFilters.length; i++) {
                rotationFilters[i] = new OneEuroFilter(
                        minCutoff,
                        beta,
                        derivateCutoff
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Pose filterRotation(Pose pose) {

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        double timestamp = ((double) (System.currentTimeMillis() - startTime)) / 1000;

        float[] rotations = pose.getRotationQuaternion();
        float[] translations = pose.getTranslation();

        float[] smoothedRotations = new float[4];
        try {
            smoothedRotations[0] = (float) rotationFilters[0].filter(rotations[0], timestamp);
            smoothedRotations[1] = (float) rotationFilters[1].filter(rotations[1], timestamp);
            smoothedRotations[2] = (float) rotationFilters[2].filter(rotations[2], timestamp);
            smoothedRotations[3] = (float) rotationFilters[3].filter(rotations[3], timestamp);

            return new Pose(translations, smoothedRotations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Pose filterTranslation(Pose pose) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        double timestamp = ((double) (System.currentTimeMillis() - startTime)) / 1000;

        float[] rotations = pose.getRotationQuaternion();
        float[] translations = pose.getTranslation();

        float[] smoothedTranslations = new float[3];
        try {
            smoothedTranslations[0] = (float) translationFilters[0].filter(translations[0], timestamp);
            smoothedTranslations[1] = (float) translationFilters[1].filter(translations[1], timestamp);
            smoothedTranslations[2] = (float) translationFilters[2].filter(translations[2], timestamp);

            return new Pose(smoothedTranslations, rotations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Pose filter(Pose pose) {

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        double timestamp = ((double) (System.currentTimeMillis() - startTime)) / 1000;

        float[] rotations = pose.getRotationQuaternion();
        float[] translations = pose.getTranslation();

        float[] smoothedTranslations = new float[3];
        float[] smoothedRotations = new float[4];
        try {
            smoothedRotations[0] = (float) rotationFilters[0].filter(rotations[0], timestamp);
            smoothedRotations[1] = (float) rotationFilters[1].filter(rotations[1], timestamp);
            smoothedRotations[2] = (float) rotationFilters[2].filter(rotations[2], timestamp);
            smoothedRotations[3] = (float) rotationFilters[3].filter(rotations[3], timestamp);

//            smoothedTranslations[0] = (float) translationFilters[0].filter(translations[0], timestamp);
//            smoothedTranslations[1] = (float) translationFilters[1].filter(translations[1], timestamp);
//            smoothedTranslations[2] = (float) translationFilters[2].filter(translations[2], timestamp);

            return new Pose(translations, smoothedRotations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
