package ai.fritz.vision.imagesegmentation;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

public class MaskClass {

    public static MaskClass NONE = new MaskClass("None", Color.TRANSPARENT);
    public static MaskClass BUILDING_EDIFICE = new MaskClass("Building. Edifice", Color.GRAY);
    public static MaskClass SKY = new MaskClass("Sky", Color.RED);
    public static MaskClass TREE = new MaskClass("Tree", Color.parseColor("#4ca64c")); // mid green
    public static MaskClass SIDEWALK_PAVEMENT = new MaskClass("Sidewalk, Pavement", Color.DKGRAY);
    public static MaskClass EARTH_GROUND = new MaskClass("Earth, Ground", Color.parseColor("#004000")); // dark green
    public static MaskClass CAR = new MaskClass("Car, Auto, Automobile, Machine, Motorcar", Color.parseColor("#FFDB99")); //  light orange
    public static MaskClass WATER = new MaskClass("Water", Color.BLUE);
    public static MaskClass HOUSE = new MaskClass("House", Color.parseColor("#A64CA6")); // purple
    public static MaskClass FENCE = new MaskClass("Fence, Fencing", Color.WHITE);
    public static MaskClass SIGNBOARD = new MaskClass("Signboard, Sign", Color.parseColor("#FFC0CB")); // pink
    public static MaskClass SKYSCRAPER = new MaskClass("Skyscraper", Color.LTGRAY);
    public static MaskClass BRIDGE = new MaskClass("Bridge, Span", Color.parseColor("#F4A460")); // orange
    public static MaskClass RIVER = new MaskClass("River", Color.parseColor("#6666ff")); // mid blue
    public static MaskClass BUS = new MaskClass("bus, autobus, coach, charabanc, double-decker, jitney, motorbus, motorcoach, omnibus, passenger vehicle", Color.parseColor("#ffa500")); // dark orange
    public static MaskClass TRUCK = new MaskClass("truck, motortruck", Color.parseColor("#332100")); // dark brown
    public static MaskClass VAN = new MaskClass("van", Color.parseColor("#FFC04C")); // normal orange
    public static MaskClass MINIBIKE = new MaskClass("minibike, motorbike", Color.BLACK);
    public static MaskClass BICYCLE = new MaskClass("bicycle, bike, wheel, cycle", Color.parseColor("#334045")); // dark blue
    public static MaskClass TRAFFIC_LIGHT = new MaskClass("traffic light, traffic signal, stoplight", Color.YELLOW);
    public static MaskClass PERSON = new MaskClass("person, individual, someone, somebody, mortal, soul", Color.CYAN);

    public static MaskClass CHAIR = new MaskClass("Chair", Color.parseColor("#F4A460")); // Sandy Brown
    public static MaskClass WALL = new MaskClass("Wall", Color.WHITE);
    public static MaskClass COFFEE_TABLE = new MaskClass("Coffee Table", Color.parseColor("#A52A2A")); // Brown
    public static MaskClass CEILING = new MaskClass("Ceiling", Color.LTGRAY);
    public static MaskClass FLOOR = new MaskClass("Floor", Color.DKGRAY);
    public static MaskClass BED = new MaskClass("Bed", Color.parseColor("#add8e6")); // Light Blue
    public static MaskClass LAMP = new MaskClass("Lamp", Color.YELLOW);
    public static MaskClass SOFA = new MaskClass("Sofa", Color.RED);
    public static MaskClass WINDOW = new MaskClass("Window", Color.CYAN);
    public static MaskClass PILLOW = new MaskClass("Pillow", Color.parseColor("#FFE4C4")); // beige

    public static MaskClass HAIR = new MaskClass("Hair", Color.RED);
    public static MaskClass PET = new MaskClass("Pet", Color.BLUE);

    public String label;
    public int color;

    /**
     * Constructor for MaskClass.
     *
     * @param label
     * @param color
     * @hide
     */
    public MaskClass(String label, int color) {
        this.label = label;
        this.color = color;
    }

    /**
     * The color to use for the mask.
     *
     * @return the color value
     * @hide
     */
    public int getColorIdentifier() {
        return color;
    }

    /**
     * The label for the mask.
     *
     * @return a string
     * @hide
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the color you'd like for this mask type.
     *
     * @param color
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Checks if the custom model are equal.
     *
     * @param other The other MaskClass
     * @return true if equal, false otherwise
     * @hide
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        MaskClass maskClass = (MaskClass) other;
        return this.hashCode() == maskClass.hashCode();
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
