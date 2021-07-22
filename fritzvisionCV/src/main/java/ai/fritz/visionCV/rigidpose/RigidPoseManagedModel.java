package ai.fritz.visionCV.rigidpose;

import org.opencv.core.Point3;

import java.util.List;

import ai.fritz.core.FritzManagedModel;

/**
 * Managed Model for RigidPose Pose Managed Model.
 */
public class RigidPoseManagedModel extends FritzManagedModel {

    private int numKeypoints;
    private List<Point3> object3DPoints;

    public RigidPoseManagedModel(String modelId, int numKeypoints,
                                 List<Point3> object3DPoints) {
        super(modelId);
        this.numKeypoints = numKeypoints;
        this.object3DPoints = object3DPoints;
    }

    public int getNumKeypoints() {
        return numKeypoints;
    }

    public List<Point3> getObject3DPoints() {
        return object3DPoints;
    }
}
