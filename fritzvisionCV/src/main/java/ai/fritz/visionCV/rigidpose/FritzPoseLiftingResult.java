package ai.fritz.visionCV.rigidpose;

import com.google.ar.core.Pose;

import org.opencv.core.Mat;

public class FritzPoseLiftingResult {

    private Pose pose;
    private Mat rotationVector;
    private Mat translationVector;

    public FritzPoseLiftingResult(Pose pose, Mat translationVector, Mat rotationVector) {
        this.pose = pose;
        this.translationVector = translationVector;
        this.rotationVector = rotationVector;
    }

    /**
     * Get an ARCore Pose that you can set on an object.
     * @return an ARCore Pose object
     */
    public Pose getARPose() {
        return pose;
    }

    public Mat getRotationVector() {
        return rotationVector;
    }

    public Mat getTranslationVector() {
        return translationVector;
    }

    public float getRotationAxisAngle() {
        float xr = (float) rotationVector.get(0, 0)[0];
        float yr = (float) rotationVector.get(1, 0)[0];
        float zr = (float) rotationVector.get(2, 0)[0];

        return (float) (Math.sqrt(xr * xr + yr * yr + zr *zr) * 180 / Math.PI);
    }
}
