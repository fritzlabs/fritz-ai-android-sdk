package ai.fritz.sdktests.validators

import ai.fritz.vision.poseestimation.FritzVisionPoseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue


class PoseResultValidator(private val poseResult: FritzVisionPoseResult) {
    fun assertHasAtLeastOnePose() {
        assertTrue(poseResult.poses.size > 0);
    }

    fun assertNumPoses(numPoses: Int) {
        assertEquals(numPoses, poseResult.poses.size);
    }
}