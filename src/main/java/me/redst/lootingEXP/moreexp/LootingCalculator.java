package me.redst.lootingEXP.moreexp;

import me.redst.lootingEXP.config.RoundingMode;

public final class LootingCalculator {

    private static final long SCALE = 10_000L;
    private static final long BONUS_UNITS_LIMIT = 1_000_000_000L;

    private LootingCalculator() {
    }

    public static int apply(int baseXp,
                            int lootingLevel,
                            long bonusHundredths,
                            int maxXpPerKill,
                            RoundingMode rounding) {
        if (baseXp <= 0 || lootingLevel <= 0 || bonusHundredths <= 0L) {
            return baseXp;
        }

        long bonusUnits = (long) baseXp * (long) lootingLevel;
        if (bonusUnits > BONUS_UNITS_LIMIT) {
            return Math.max(baseXp, maxXpPerKill);
        }

        long scaledTotal = (long) baseXp * SCALE + bonusUnits * bonusHundredths;

        long result = switch (rounding) {
            case NEAREST -> (scaledTotal + SCALE / 2L) / SCALE;
            case DOWN -> scaledTotal / SCALE;
            case UP -> (scaledTotal + SCALE - 1L) / SCALE;
        };

        if (result > maxXpPerKill) {
            result = maxXpPerKill;
        }
        if (result < baseXp) {
            result = baseXp;
        }
        return (int) result;
    }
}
