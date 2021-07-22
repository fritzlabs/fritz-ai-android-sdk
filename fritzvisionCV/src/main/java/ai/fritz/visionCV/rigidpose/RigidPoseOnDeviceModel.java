package ai.fritz.visionCV.rigidpose;

import org.opencv.core.Point3;

import java.util.List;

import ai.fritz.core.FritzOnDeviceModel;

/**
 * On Device Model class for a RigidPose Pose Model
 */
public class RigidPoseOnDeviceModel extends FritzOnDeviceModel {

    private int numKeypoints;
    private List<Point3> object3DPoints;

    public RigidPoseOnDeviceModel(String modelPath, String modelId, int version, int numKeypoints,
                                  List<Point3> object3DPoints) {
        super(modelPath, modelId, version);
        this.numKeypoints = numKeypoints;
        this.object3DPoints = object3DPoints;
    }

    public RigidPoseOnDeviceModel(String modelPath, String modelId, int version, RigidPoseManagedModel managedModel) {
        super(modelPath, modelId, version);
        this.numKeypoints = managedModel.getNumKeypoints();
        this.object3DPoints = managedModel.getObject3DPoints();
    }

    public int getNumKeypoints() {
        return numKeypoints;
    }

    public List<Point3> getObject3DPoints() {
        return object3DPoints;
    }
}
