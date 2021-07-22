package ai.fritz.visionCV.rigidpose;

import android.util.Pair;

import com.google.ar.core.Camera;
import com.google.ar.core.CameraIntrinsics;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.ArrayList;
import java.util.List;

public class FritzVisionRigidPoseLifting {

    private Point[] predicted2DPoints;
    private List<Integer> keypointsToExclude;

    private MatOfPoint3f object3DPoints;

    public static Mat getCameraIntrinsicMatrix(Camera camera) {
        CameraIntrinsics imgIntrinsics = camera.getImageIntrinsics();
        float[] focalLengths = imgIntrinsics.getFocalLength();
        float[] principalPoints = imgIntrinsics.getPrincipalPoint();

        Mat cameraMatrix = new Mat(3, 3, CvType.CV_32FC1);

        cameraMatrix.put(0, 0,
                focalLengths[1], 0.0, principalPoints[1],
                0.0, focalLengths[0], principalPoints[0],
                0.00000, 0.00000, 1.00000
        );

        return cameraMatrix;
    }

    public static MatOfDouble getDistortionMatrix() {
        MatOfDouble distorsionMatrix = new MatOfDouble();
        double[] distArray = {0.0,
                0.0,
                0.0,
                0.0,
                0.0
        };
        distorsionMatrix.fromArray(distArray);

        return distorsionMatrix;
    }

    public FritzVisionRigidPoseLifting(Point[] predicted2DPoints, List<Point3> object3DPoints) {
        this(predicted2DPoints, object3DPoints, new ArrayList<Integer>());
    }

    public FritzVisionRigidPoseLifting(Point[] predicted2DPoints, List<Point3> objPoints, List<Integer> keypointsToExclude) {
        this.predicted2DPoints = predicted2DPoints;
        this.keypointsToExclude = keypointsToExclude;

        int index = 0;
        for (Point3 objPoint : objPoints) {
            if (keypointsToExclude.contains(index++)) {
                objPoints.remove(objPoint);
            }
        }

        object3DPoints = new MatOfPoint3f();
        object3DPoints.fromList(objPoints);
    }

    public FritzPoseLiftingResult infer3DPose(Mat cameraMatrix, MatOfDouble distortionMatrix) {
        return infer3DPose(cameraMatrix, distortionMatrix, null);
    }

    public FritzPoseLiftingResult infer3DPose(Mat cameraMatrix, MatOfDouble distortionMatrix, FritzPoseLiftingResult priorPoseResult) {
        Pair<Mat, Mat> result = calculateTranslationAndRotation(cameraMatrix, distortionMatrix, priorPoseResult);

        Mat translationVector = result.first;
        Mat rotationVector = result.second;

        float xt = (float) translationVector.get(0, 0)[0];
        float yt = (float) translationVector.get(1, 0)[0];
        float zt = (float) translationVector.get(2, 0)[0];

        float[] translation = {xt, -yt, -zt};

        Quaternion quaternion = calculatePoseRotation(rotationVector);

        float[] quatValues = {quaternion.x, quaternion.y, quaternion.z, quaternion.w};

        Pose pose = new Pose(translation, quatValues);

        return new FritzPoseLiftingResult(pose, translationVector, rotationVector);
    }

    private Quaternion calculatePoseRotation(Mat rotationVector) {
        float xr = (float) rotationVector.get(0, 0)[0];
        float yr = (float) rotationVector.get(1, 0)[0];
        float zr = (float) rotationVector.get(2, 0)[0];

        float theta = (float) (Math.sqrt(xr * xr + yr * yr + zr * zr) * 180 / Math.PI);
        Vector3 axis = new Vector3(xr, -yr, -zr);

        Quaternion quaternion = Quaternion.axisAngle(axis, theta);
        return quaternion;
    }

    private Pair<Mat, Mat> calculateTranslationAndRotation(Mat camMatrix, MatOfDouble distCoeffs, FritzPoseLiftingResult priorPoseResult) {
        List<Point> pointsToInclude = new ArrayList<>();
        for (int i = 0; i < predicted2DPoints.length; i++) {
            Point scaledPoint = predicted2DPoints[i];
            if (!keypointsToExclude.contains(i)) {
                pointsToInclude.add(scaledPoint);
            }
        }

        MatOfPoint2f imagePoints = new MatOfPoint2f();
        imagePoints.fromList(pointsToInclude);

        Mat inliers = new Mat();

        boolean useExtrinsicGuess = false;
        Mat rotationVector, translationVector;
        if (priorPoseResult != null) {
            rotationVector = priorPoseResult.getRotationVector();
            translationVector = priorPoseResult.getTranslationVector();
            useExtrinsicGuess = true;
        } else {
            rotationVector = new Mat(3, 1, CvType.CV_64FC1);
            translationVector = new Mat(3, 1, CvType.CV_64FC1);
        }
        Calib3d.solvePnPRansac(object3DPoints, imagePoints, camMatrix, distCoeffs, rotationVector, translationVector,
                useExtrinsicGuess, 150,
                1,
                0.85,
                inliers,
                Calib3d.SOLVEPNP_ITERATIVE);

        return new Pair<>(translationVector, rotationVector);
    }
}
