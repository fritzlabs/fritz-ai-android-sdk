package ai.fritz.sdktests;

public enum TestingAsset {

    HAND("hand.jpg"),
    TODDLER("toddler.jpg"),
    PERSON("person.jpg"),
    LIVING_ROOM("living_room.png"),
    OUTDOOR("outdoor.jpg"),
    FAMILY("family.png"),
    FAMILY_ROTATE_270("family_rotate_270.png"),
    CAT("cat.jpg"),
    GIRL("girl.jpg"),
    SKY("clear-blue-sky.jpg"),
    DENTASTIX("dentastix.png"),
    CLIMBING("climbing.png"),
    TIGER("tiger.jpg"),
    STEVEN_VIDEO("steven_face.mp4"),
    SOCCER_BALLS("soccer_balls.jpg");

    String path;

    TestingAsset(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
