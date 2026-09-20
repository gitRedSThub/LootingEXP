package me.redst.lootingEXP.config;

public final class LootingExpConfig {

    public static final String PATH_ENABLED = "more-exp.enabled";
    public static final boolean DEFAULT_ENABLED = true;

    public static final String PATH_PERCENT = "more-exp.exp-per-looting-level-percent";
    public static final long PERCENT_MIN_HUNDREDTHS = 0L;
    public static final long PERCENT_MAX_HUNDREDTHS = 100_000L;
    public static final long PERCENT_DEFAULT_HUNDREDTHS = 3_000L;

    public static final String PATH_MAX_XP = "more-exp.max-xp-per-kill";
    public static final int MAX_XP_MIN = 1;
    public static final int MAX_XP_MAX = 100_000;
    public static final int MAX_XP_DEFAULT = 10_000;

    public static final String PATH_ROUNDING_MODE = "more-exp.rounding.mode";
    public static final RoundingMode ROUNDING_DEFAULT = RoundingMode.NEAREST;

    private final boolean moreExpEnabled;
    private final long percentHundredths;
    private final int maxXpPerKill;
    private final RoundingMode roundingMode;
    private final boolean moreExpActive;

    public LootingExpConfig(boolean moreExpEnabled,
                            long percentHundredths,
                            int maxXpPerKill,
                            RoundingMode roundingMode) {
        this.moreExpEnabled = moreExpEnabled;
        this.percentHundredths = percentHundredths;
        this.maxXpPerKill = maxXpPerKill;
        this.roundingMode = roundingMode;
        this.moreExpActive = moreExpEnabled && percentHundredths > 0L;
    }

    public static LootingExpConfig defaults() {
        return new LootingExpConfig(DEFAULT_ENABLED,
                PERCENT_DEFAULT_HUNDREDTHS,
                MAX_XP_DEFAULT,
                ROUNDING_DEFAULT);
    }

    public boolean isMoreExpEnabled() {
        return this.moreExpEnabled;
    }

    public boolean isMoreExpActive() {
        return this.moreExpActive;
    }

    public long percentHundredths() {
        return this.percentHundredths;
    }

    public int maxXpPerKill() {
        return this.maxXpPerKill;
    }

    public RoundingMode roundingMode() {
        return this.roundingMode;
    }
}
