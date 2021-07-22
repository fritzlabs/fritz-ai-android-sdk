package ai.fritz.vision;

public enum ImageRotation {
    ROTATE_0(0),
    ROTATE_90(90),
    ROTATE_180(180),
    ROTATE_270(270);

    int rotation;

    ImageRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    public static ImageRotation getFromValue(int value) {
        for (ImageRotation imageRotation : ImageRotation.values()) {
            if (imageRotation.getRotation() == value) {
                return imageRotation;
            }
        }

        return null;
    }
}
