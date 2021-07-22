package ai.fritz.vision.poseestimation;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Size;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Pose postprocessing on raw model outputs.
 *
 * @hide
 */
public class PoseDecoderWithDisplacements {

    private static final String TAG = PoseDecoderWithDisplacements.class.getSimpleName();
    private static final int DEFAULT_QUEUE_SIZE = 25;

    PriorityBlockingQueue<PartScore> scoreQueue = new PriorityBlockingQueue<PartScore>(DEFAULT_QUEUE_SIZE, new Comparator<PartScore>() {
        @Override
        public int compare(PartScore partScore1, PartScore partScore2) {
            if (partScore1.getScore() > partScore2.getScore()) {
                return 1;
            }
            return -1;
        }
    });

    private HeatmapScores heatmapScores;
    private Offsets offsets;
    private Displacements displacementsFwd;
    private Displacements displacementsBwd;
    private Size bounds;
    private Skeleton skeleton;

    public PoseDecoderWithDisplacements(
            HeatmapScores heatmapScores,
            Offsets offsets,
            Displacements displacementsFwd,
            Displacements displacementsBwd,
            Size bounds,
            Skeleton skeleton) {
        this.heatmapScores = heatmapScores;
        this.offsets = offsets;
        this.displacementsFwd = displacementsFwd;
        this.displacementsBwd = displacementsBwd;
        this.bounds = bounds;
        this.skeleton = skeleton;
    }

    public List<Pose> decodeMultiplePoses(int outputStride, int maxPoseDetections, float scoreThreshold, float nmsRadius, int localMaxRadius) {
        List<Pose> poses = new ArrayList<>();
        float squaredNMSRadius = nmsRadius * nmsRadius;
        PriorityBlockingQueue<PartScore> scoreQueue = buildPartWithScoringQueue(scoreThreshold, localMaxRadius);

        while (poses.size() < maxPoseDetections && !scoreQueue.isEmpty()) {
            PartScore root = scoreQueue.poll();

            PointF rootImageCoordinates = getImageCoordinates(root, outputStride);

            if (withinNMSRadiusOfCorrespondingPoint(poses, squaredNMSRadius, rootImageCoordinates, root.getKeypointId())) {
                continue;
            }

            Keypoint[] keypoints = decodePose(root, outputStride);
            float poseScore = getInstanceScore(poses, squaredNMSRadius, keypoints);
            poses.add(new Pose(skeleton, keypoints, poseScore, scoreThreshold, bounds));
        }

        return poses;
    }

    private Keypoint[] decodePose(PartScore root, int outputStride) {
        int numParts = skeleton.getNumKeypoints();
        int numEdges = skeleton.getNumEdges();
        Keypoint[] instanceKeypoints = new Keypoint[numParts];
        PointF rootCoord = getImageCoordinates(root, outputStride);

        int keypointId = root.getKeypointId();
        String keypointName = skeleton.getKeypointName(keypointId);
        instanceKeypoints[root.getKeypointId()] = new Keypoint(keypointId, keypointName, rootCoord, root.getScore(), bounds);

        // Decode the part positions upwards in the tree, following the backward
        // displacements.
        Integer[] parentToChildEdges = skeleton.getParentToChildEdges();
        Integer[] childToParentEdges = skeleton.getChildToParentEdges();
        for (int edgeIndex = numEdges - 1; edgeIndex >= 0; edgeIndex--) {
            int sourceKeypointId = parentToChildEdges[edgeIndex];
            int targetKeypointId = childToParentEdges[edgeIndex];

            if (instanceKeypoints[sourceKeypointId] != null && instanceKeypoints[targetKeypointId] == null) {
                instanceKeypoints[targetKeypointId] = traverseToTargetKeypoint(displacementsBwd, edgeIndex, instanceKeypoints[sourceKeypointId], targetKeypointId, outputStride);
            }
        }

        // Decode the part positions upwards in the tree, following the forward
        // displacements.

        for (int edgeIndex = 0; edgeIndex < numEdges; edgeIndex++) {
            int sourceKeypointId = childToParentEdges[edgeIndex];
            int targetKeypointId = parentToChildEdges[edgeIndex];

            if (instanceKeypoints[sourceKeypointId] != null && instanceKeypoints[targetKeypointId] == null) {
                instanceKeypoints[targetKeypointId] = traverseToTargetKeypoint(displacementsFwd, edgeIndex, instanceKeypoints[sourceKeypointId], targetKeypointId, outputStride);
            }
        }

        return instanceKeypoints;
    }

    private PriorityBlockingQueue buildPartWithScoringQueue(float threshold, int localMaxRadius) {

        for (int x = 0; x < heatmapScores.getWidth(); x++) {
            for (int y = 0; y < heatmapScores.getHeight(); y++) {
                for (int keypointIndex = 0; keypointIndex < heatmapScores.getNumKeypoints(); keypointIndex++) {
                    float scoreForKeypoint = heatmapScores.getScore(keypointIndex, x, y);

                    if (scoreForKeypoint < threshold) {
                        continue;
                    }

                    if (scoreIsMaximumInLocalWindow(keypointIndex, x, y, scoreForKeypoint, localMaxRadius)) {
                        scoreQueue.put(new PartScore(keypointIndex, x, y, scoreForKeypoint));
                    }
                }
            }
        }

        return scoreQueue;
    }

    private Keypoint traverseToTargetKeypoint(Displacements displacements, int edgeIndex, Keypoint sourceKeypoint, int targetKeypointId, int outputStride) {
        Point sourceKeypointIndicies = getStridedIndexNearPoint(sourceKeypoint.getPosition(), outputStride, heatmapScores.getHeight(), heatmapScores.getWidth());
        PointF displacement = displacements.getDisplacement(edgeIndex, sourceKeypointIndicies.x, sourceKeypointIndicies.y);

        PointF displacedPoint = addVectors(sourceKeypoint.getPosition(), displacement);
        Point displacedPointIndices = getStridedIndexNearPoint(displacedPoint, outputStride, heatmapScores.getHeight(), heatmapScores.getWidth());
        PointF offsetPoint = offsets.getOffsetPoint(targetKeypointId, displacedPointIndices.x, displacedPointIndices.y);

        float score = heatmapScores.getScore(targetKeypointId, displacedPointIndices.x, displacedPointIndices.y);

        PointF targetKeypointPosition = addVectors(new PointF(displacedPointIndices.x * outputStride, displacedPointIndices.y * outputStride), offsetPoint);
        String keypointName = skeleton.getKeypointName(targetKeypointId);

        return new Keypoint(targetKeypointId, keypointName, targetKeypointPosition, score, bounds);
    }

    private float getInstanceScore(List<Pose> poses, float squaredNMSRadius, Keypoint[] instanceKeypoints) {
        float instanceScore = 0;

        for (Keypoint keypoint : instanceKeypoints) {
            if (keypoint == null) {
                continue;
            }

            if (!withinNMSRadiusOfCorrespondingPoint(poses, squaredNMSRadius, keypoint.getPosition(), keypoint.getId())) {
                instanceScore += keypoint.getScore();
            }
        }

        return instanceScore / instanceKeypoints.length;
    }


    private PointF addVectors(PointF point1, PointF point2) {
        return new PointF(point1.x + point2.x, point1.y + point2.y);
    }

    private Point getStridedIndexNearPoint(PointF point, int outputStride, int height, int width) {
        int xRounded = Math.round(point.x / outputStride);
        int yRounded = Math.round(point.y / outputStride);

        return new Point(clamp(xRounded, 0, width - 1), clamp(yRounded, 0, height - 1));
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private boolean scoreIsMaximumInLocalWindow(int keypointIndex, int x, int y, float score, int localMaxRadius) {
        int yStart = Math.max(y - localMaxRadius, 0);
        int yEnd = Math.min(y + localMaxRadius, heatmapScores.getHeight());
        int xStart = Math.max(x - localMaxRadius, 0);
        int xEnd = Math.min(x + localMaxRadius, heatmapScores.getWidth());

        for (int yIndex = yStart; yIndex < yEnd; yIndex++) {
            for (int xIndex = xStart; xIndex < xEnd; xIndex++) {
                if (heatmapScores.getScore(keypointIndex, xIndex, yIndex) > score) {
                    return false;
                }
            }
        }
        return true;
    }

    private PointF getImageCoordinates(Part part, int outputStride) {
        PointF dxy = offsets.getOffsetPoint(part.getKeypointId(), part.getHeatMapScoresX(), part.getHeatMapScoresY());
        return new PointF(part.getHeatMapScoresX() * outputStride + dxy.x, part.getHeatMapScoresY() * outputStride + dxy.y);
    }

    private boolean withinNMSRadiusOfCorrespondingPoint(List<Pose> poses, double squaredNMSRadius, PointF imageCoord, int keypointId) {
        for (Pose pose : poses) {
            Keypoint keypoint = pose.getKeypoints()[keypointId];
            if (keypoint.calculateSquaredDistanceFromCoordinates(imageCoord) <= squaredNMSRadius) {
                return true;
            }
        }
        return false;
    }
}
