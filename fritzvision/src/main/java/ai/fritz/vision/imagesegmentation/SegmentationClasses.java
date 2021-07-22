package ai.fritz.vision.imagesegmentation;

public class SegmentationClasses {

    public static final MaskClass[] PEOPLE = {
            MaskClass.NONE,
            MaskClass.PERSON
    };
    public static final MaskClass[] OUTDOOR = {
            MaskClass.NONE,
            MaskClass.BUILDING_EDIFICE,
            MaskClass.SKY,
            MaskClass.TREE,
            MaskClass.SIDEWALK_PAVEMENT,
            MaskClass.EARTH_GROUND,
            MaskClass.CAR,
            MaskClass.WATER,
            MaskClass.HOUSE,
            MaskClass.FENCE,
            MaskClass.SIGNBOARD,
            MaskClass.SKYSCRAPER,
            MaskClass.BRIDGE,
            MaskClass.RIVER,
            MaskClass.BUS,
            MaskClass.TRUCK,
            MaskClass.VAN,
            MaskClass.MINIBIKE,
            MaskClass.BICYCLE,
            MaskClass.TRAFFIC_LIGHT,
            MaskClass.PERSON
    };
    public static final MaskClass[] LIVING_ROOM = {
            MaskClass.NONE,
            MaskClass.CHAIR,
            MaskClass.WALL,
            MaskClass.COFFEE_TABLE,
            MaskClass.CEILING,
            MaskClass.FLOOR,
            MaskClass.BED,
            MaskClass.LAMP,
            MaskClass.SOFA,
            MaskClass.WINDOW,
            MaskClass.PILLOW
    };
    public static final MaskClass[] HAIR = {
            MaskClass.NONE,
            MaskClass.HAIR
    };
    public static final MaskClass[] PET = {
            MaskClass.NONE,
            MaskClass.PET
    };
    public static final MaskClass[] SKY = {
            MaskClass.NONE,
            MaskClass.SKY
    };
}
