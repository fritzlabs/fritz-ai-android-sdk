package ai.fritz.vision.poseestimation;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class Skeleton {

    protected String label;
    private String[] keypointNames;
    private Pair[] connectedKeypointNames;
    private Pair[] poseChain;

    private Map<String, Integer> keypointIds;
    private Pair[] connectedKeypointIndicies;
    private Pair[] parentChildIndicies;
    public static Integer[] parentToChildEdges;
    public static Integer[] childToParentEdges;

    public Skeleton(String label, String[] keypointNames) {
        this(label, keypointNames, new Pair[0], buildPoseChainFromKeypoints(keypointNames));
    }

    public Skeleton(String label, String[] keypointNames, Pair[] connectedKeypointNames, Pair[] poseChain) {
        this.label = label;
        this.keypointNames = keypointNames;
        this.connectedKeypointNames = connectedKeypointNames;
        this.poseChain = poseChain;

        this.keypointIds = new HashMap<>();
        for (int i = 0; i < keypointNames.length; i++) {
            String part = keypointNames[i];
            this.keypointIds.put(part, i);
        }

        this.connectedKeypointIndicies = new Pair[connectedKeypointNames.length];
        for (int i = 0; i < connectedKeypointNames.length; i++) {
            Pair<String, String> connectedPart = connectedKeypointNames[i];
            String jointA = connectedPart.first;
            String jointB = connectedPart.second;
            connectedKeypointIndicies[i] = new Pair<>(keypointIds.get(jointA), keypointIds.get(jointB));
        }

        this.parentChildIndicies = new Pair[poseChain.length];
        for (int i = 0; i < poseChain.length; i++) {
            Pair<String, String> pose = poseChain[i];
            String jointA = pose.first;
            String jointB = pose.second;
            parentChildIndicies[i] = new Pair<>(keypointIds.get(jointA), keypointIds.get(jointB));
        }

        this.parentToChildEdges = new Integer[parentChildIndicies.length];
        for (int i = 0; i < parentChildIndicies.length; i++) {
            Pair<Integer, Integer> parentChildEdge = parentChildIndicies[i];
            parentToChildEdges[i] = parentChildEdge.second;
        }

        this.childToParentEdges = new Integer[parentChildIndicies.length];
        for (int i = 0; i < parentChildIndicies.length; i++) {
            Pair<Integer, Integer> parentChildEdge = parentChildIndicies[i];
            childToParentEdges[i] = parentChildEdge.first;
        }
    }

    public String getLabel() {
        return label;
    }

    public String[] getKeypointNames() {
        return keypointNames;
    }

    public int getNumKeypoints() {
        return keypointNames.length;
    }

    public String getKeypointName(int index) {
        return keypointNames[index];
    }

    public int getNumEdges() {
        return parentToChildEdges.length;
    }

    public Integer[] getChildToParentEdges() {
        return childToParentEdges;
    }

    public Integer[] getParentToChildEdges() {
        return parentToChildEdges;
    }

    public Pair[] getConnectedKeypointNames() {
        return connectedKeypointNames;
    }

    public Pair[] getConnectedKeypointIndicies() {
        return connectedKeypointIndicies;
    }

    public Pair[] getPoseChain() {
        return poseChain;
    }

    private static Pair[] buildPoseChainFromKeypoints(String[] keypointNames) {
        Pair[] poseChain = new Pair[keypointNames.length - 1];
        for (int kidx = 0; kidx < keypointNames.length - 1; kidx++) {
            poseChain[kidx] = new Pair<>(keypointNames[kidx], keypointNames[kidx + 1]);
        }
        return poseChain;
    }
}
