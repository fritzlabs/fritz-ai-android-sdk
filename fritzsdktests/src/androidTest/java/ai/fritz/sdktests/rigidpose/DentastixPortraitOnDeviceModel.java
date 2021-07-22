package ai.fritz.sdktests.rigidpose;

import org.opencv.core.Point3;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.visionCV.rigidpose.RigidPoseOnDeviceModel;

public class DentastixPortraitOnDeviceModel extends RigidPoseOnDeviceModel {

    private static final float LENGTH_METERS = .08f;
    private static final float HEIGHT_METERS = .014f;
    private static final float WIDTH_METERS = .014f;

    private static final double HALF_LENGTH = LENGTH_METERS / 2.0;
    private static final double HALF_HEIGHT = HEIGHT_METERS / 2.0;
    private static final double HALF_WIDTH = WIDTH_METERS / 2.0;

    private static final String MODEL_PATH = "file:///android_asset/dentastix_chris_260x200_35_small_1560877588.tflite";
    private static final String MODEL_ID = "791304461a6d4b398061a8b3914609cc";
    private static final int INPUT_HEIGHT = 260;
    private static final int INPUT_WIDTH = 200;

    private static final int OUTPUT_HEIGHT = 17;
    private static final int OUTPUT_WIDTH = 13;
    private static final int NUM_PARTS = 5;

    private static List<Point3> createObject3DPoints() {
        List<Point3> objPoints = new ArrayList<Point3>();

        // Top Left
        objPoints.add(new Point3(HALF_LENGTH, HALF_HEIGHT, HALF_WIDTH));

        // Bottom Left
        objPoints.add(new Point3(HALF_LENGTH, -HALF_HEIGHT, -HALF_WIDTH));

        // Top Right
        objPoints.add(new Point3(-HALF_LENGTH, HALF_HEIGHT, HALF_WIDTH));

        // Bottom Right
        objPoints.add(new Point3(-HALF_LENGTH, -HALF_HEIGHT, -HALF_WIDTH));
        objPoints.add(new Point3(0, 0, 0));
        return objPoints;
    }

    public DentastixPortraitOnDeviceModel() {
        super(MODEL_PATH, MODEL_ID, 31,
                NUM_PARTS, createObject3DPoints());
    }
}
