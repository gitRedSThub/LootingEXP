package me.redst.lootingEXP.config;

public enum RoundingMode {

    NEAREST("Nearest"),
    DOWN("Down"),
    UP("Up");

    private static final RoundingMode[] VALUES = values();

    private final String displayName;

    RoundingMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return this.displayName;
    }

    public static RoundingMode parse(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        for (RoundingMode mode : VALUES) {
            if (mode.name().equalsIgnoreCase(trimmed)) {
                return mode;
            }
        }
        return null;
    }
}
