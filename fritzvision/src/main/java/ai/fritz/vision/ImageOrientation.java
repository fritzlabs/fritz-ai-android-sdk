package ai.fritz.vision;

public enum ImageOrientation {
    UP(0, false, false),
    UP_MIRRORED(0, true, false),
    DOWN(180, false, false),
    DOWN_MIRRORED(180, true, false),
    LEFT(270, false, false),
    LEFT_MIRRORED(90, true, false),
    RIGHT(90, false, false),
    RIGHT_MIRRORED(270, true, false);

    int rotation;
    boolean flipHorizontal;
    boolean flipVertical;

    ImageOrientation(int rotation, boolean flipHorizontal, boolean flipVertical) {
        this.rotation = rotation;
        this.flipHorizontal = flipHorizontal;
        this.flipVertical = flipVertical;
    }

    public int getRotation() {
        return rotation;
    }

    public boolean getFlipHorizontal() {
        return flipHorizontal;
    }

    public boolean getFlipVertical() {
        return flipVertical;
    }

    public static ImageOrientation getOrientationFromName(String name, boolean mirrored) {
        if(mirrored) {
            name = name + "_MIRRORED";
        }

        for(ImageOrientation imageOrientation : ImageOrientation.values()) {
            if(imageOrientation.name().equalsIgnoreCase(name)) {
                return imageOrientation;
            }
        }

        return null;
    }
}
