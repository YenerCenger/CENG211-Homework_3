package enums;

public enum FoodType {
    KRILL("Kr"),
    CRUSTACEAN("Cr"),
    ANCHOVY("An"),
    SQUID("Sq"),
    MACKEREL("Ma");

    private final String shortName;

    FoodType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}