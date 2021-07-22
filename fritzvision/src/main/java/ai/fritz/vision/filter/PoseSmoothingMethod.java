package ai.fritz.vision.filter;

public interface PoseSmoothingMethod {
    String getName();

    PoseSmoother buildPoseSmoother(int numParts);
}
