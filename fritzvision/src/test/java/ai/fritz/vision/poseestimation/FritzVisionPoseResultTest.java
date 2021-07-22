package ai.fritz.vision.poseestimation;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Size;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.ImageOrientation;
import ai.fritz.vision.ImageRotation;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class FritzVisionPoseResultTest {

    @Test
    public void testUpdatePoseLocation() {
        // Create test keypoints
        Keypoint[] keypoints = new Keypoint[2];

        Skeleton skeleton = new HumanSkeleton();

        Size modelSize = new Size(100, 100);
        keypoints[0] = new Keypoint(0, skeleton.getKeypointName(0), new PointF(10, 20), .1f, modelSize);
        keypoints[1] = new Keypoint(1, skeleton.getKeypointName(1), new PointF(50, 100), .1f, modelSize);

        // Create the pose
        Pose pose = new Pose(skeleton, keypoints, .1f, .1f, modelSize);

        // Set the model size and the size to scale to
        Size scaleToSize = new Size(450, 600);

        // Scale the Keypoint positions
        Pose newPose = pose.scaledTo(scaleToSize);

        // Fetch the keypoints
        Keypoint[] scaledKeypoints = newPose.getKeypoints();
        assertEquals(keypoints.length, scaledKeypoints.length);
        PointF keypoint0Position = scaledKeypoints[0].getPosition();
        PointF keypoint1Position = scaledKeypoints[1].getPosition();

        // Assert the keypoint positions are updated to the size specified.
        float delta = .1f;
        // Check the first keypoint position is scaled (scale factor is 4.5 and 6)
        assertEquals(keypoint0Position.x, 10 * 4.5, delta);
        assertEquals(keypoint0Position.y, 20 * 6, delta);

        // scale factor is 4.5 and 6
        assertEquals(keypoint1Position.x, 50 * 4.5, delta);
        assertEquals(keypoint1Position.y, 100 * 6, delta);
    }

    @Test
    public void testGetPoses() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        List<Pose> poses = new ArrayList<>();
        Skeleton skeleton = new HumanSkeleton();

        float poseThreshold = .1f;
        float keypointThreshold = .1f;
        Size bounds = new Size(224, 224);

        // Above Pose Threshold
        Keypoint[] keypoints = new Keypoint[2];
        keypoints[0] = new Keypoint(0, skeleton.getKeypointName(0), new PointF(10, 20), keypointThreshold + .01f, bounds);
        keypoints[1] = new Keypoint(1, skeleton.getKeypointName(1), new PointF(50, 100), keypointThreshold + .01f, bounds);
        poses.add(new Pose(skeleton, keypoints, poseThreshold + .01f, keypointThreshold, bounds));

        // Below Pose Threshold
        Keypoint[] keypoints3 = new Keypoint[2];
        keypoints3[0] = new Keypoint(0, skeleton.getKeypointName(0), new PointF(10, 20), keypointThreshold + .01f, bounds);
        keypoints3[1] = new Keypoint(1, skeleton.getKeypointName(1), new PointF(50, 100), keypointThreshold + .01f, bounds);
        poses.add(new Pose(skeleton, keypoints3, poseThreshold - .01f, keypointThreshold, bounds));

        FritzVisionPoseResult poseResult = new FritzVisionPoseResult(poses, poseThreshold, bounds, visionImage.encodedSize());
        List<Pose> result = poseResult.getPoses();

        // 1 pose are above the pose threshold
        assertEquals(result.size(), 1);
    }

    @Test
    public void testGetPosesWithConfidence() {
        Bitmap bitmap = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);
        FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, ImageOrientation.RIGHT);

        List<Pose> poses = new ArrayList<>();
        Skeleton skeleton = new HumanSkeleton();

        float poseThreshold = .1f;
        float keypointThreshold = .1f;
        Size bounds = new Size(224, 224);

        // Above Pose Threshold
        Keypoint[] keypoints = new Keypoint[2];
        keypoints[0] = new Keypoint(0, skeleton.getKeypointName(0), new PointF(10, 20), keypointThreshold + .01f, bounds);
        keypoints[1] = new Keypoint(1, skeleton.getKeypointName(1), new PointF(50, 100), keypointThreshold + .01f, bounds);
        poses.add(new Pose(skeleton, keypoints, poseThreshold, keypointThreshold, bounds));

        // Below Pose Threshold
        Keypoint[] keypoints3 = new Keypoint[2];
        keypoints3[0] = new Keypoint(0, skeleton.getKeypointName(0), new PointF(10, 20), keypointThreshold + .01f, bounds);
        keypoints3[1] = new Keypoint(1, skeleton.getKeypointName(1), new PointF(50, 100), keypointThreshold + .01f, bounds);
        poses.add(new Pose(skeleton, keypoints3, poseThreshold - .01f, keypointThreshold, bounds));

        FritzVisionPoseResult poseResult = new FritzVisionPoseResult(poses, poseThreshold, bounds, visionImage.encodedSize());
        List<Pose> result = poseResult.getPosesByThreshold(poseThreshold - .01f);

        // There are 2 poses above the low pose threshold
        assertEquals(result.size(), 2);
    }
}
