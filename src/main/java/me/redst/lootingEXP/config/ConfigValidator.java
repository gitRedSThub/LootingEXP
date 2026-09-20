package me.redst.lootingEXP.config;

import me.redst.lootingEXP.util.Format;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public final class ConfigValidator {

    private ConfigValidator() {
    }

    public static LootingExpConfig validate(ConfigurationSection section, ValidationReport report) {
        boolean enabled = readBoolean(section, report);
        long percent = readPercent(section, report);
        int maxXp = readMaxXp(section, report);
        RoundingMode rounding = readRounding(section, report);
        return new LootingExpConfig(enabled, percent, maxXp, rounding);
    }

    public static void writeBack(ConfigurationSection section,
                                 LootingExpConfig config,
                                 ConfigurationSection bundledDefaults) {
        set(section, bundledDefaults, LootingExpConfig.PATH_ENABLED,
                config.isMoreExpEnabled());
        set(section, bundledDefaults, LootingExpConfig.PATH_PERCENT,
                asStoredNumber(config.percentHundredths()));
        set(section, bundledDefaults, LootingExpConfig.PATH_MAX_XP,
                config.maxXpPerKill());
        set(section, bundledDefaults, LootingExpConfig.PATH_ROUNDING_MODE,
                config.roundingMode().name());
    }

    public static Object asStoredNumber(long hundredths) {
        if (hundredths % 100L == 0L) {
            return hundredths / 100L;
        }
        return Double.parseDouble(Format.decimal(hundredths));
    }

    private static boolean readBoolean(ConfigurationSection section, ValidationReport report) {
        String path = LootingExpConfig.PATH_ENABLED;
        boolean fallback = LootingExpConfig.DEFAULT_ENABLED;
        Object raw = section.get(path);
        if (raw == null) {
            report.missing(path, String.valueOf(fallback));
            return fallback;
        }
        if (raw instanceof Boolean value) {
            return value;
        }
        if (raw instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.equalsIgnoreCase("true")) {
                return true;
            }
            if (trimmed.equalsIgnoreCase("false")) {
                return false;
            }
        }
        report.invalid(path, raw, "it must be true or false", String.valueOf(fallback));
        return fallback;
    }

    private static long readPercent(ConfigurationSection section, ValidationReport report) {
        String path = LootingExpConfig.PATH_PERCENT;
        long fallback = LootingExpConfig.PERCENT_DEFAULT_HUNDREDTHS;
        String fallbackText = Format.decimal(fallback);

        Object raw = section.get(path);
        if (raw == null) {
            report.missing(path, fallbackText);
            return fallback;
        }
        NumberParser.Result parsed = NumberParser.parse(raw);
        if (parsed.status() == NumberParser.Status.TOO_MANY_DECIMALS) {
            report.invalid(path, raw, tooManyDecimals(), fallbackText);
            return fallback;
        }
        if (!parsed.ok()) {
            report.invalid(path, raw, "it must be a number", fallbackText);
            return fallback;
        }
        long hundredths = parsed.hundredths();
        if (hundredths < LootingExpConfig.PERCENT_MIN_HUNDREDTHS
                || hundredths > LootingExpConfig.PERCENT_MAX_HUNDREDTHS) {
            report.invalid(path, raw, "the allowed range is " + percentRange(), fallbackText);
            return fallback;
        }
        return hundredths;
    }

    private static int readMaxXp(ConfigurationSection section, ValidationReport report) {
        String path = LootingExpConfig.PATH_MAX_XP;
        int fallback = LootingExpConfig.MAX_XP_DEFAULT;
        String fallbackText = Format.grouped(fallback);

        Object raw = section.get(path);
        if (raw == null) {
            report.missing(path, fallbackText);
            return fallback;
        }
        NumberParser.Result parsed = NumberParser.parse(raw);
        if (parsed.status() == NumberParser.Status.TOO_MANY_DECIMALS) {
            report.invalid(path, raw, tooManyDecimals(), fallbackText);
            return fallback;
        }
        if (!parsed.ok()) {
            report.invalid(path, raw, "it must be a number", fallbackText);
            return fallback;
        }
        long hundredths = parsed.hundredths();
        if (hundredths % 100L != 0L) {
            report.invalid(path, raw, "experience is counted in whole points", fallbackText);
            return fallback;
        }
        long whole = hundredths / 100L;
        if (whole < LootingExpConfig.MAX_XP_MIN || whole > LootingExpConfig.MAX_XP_MAX) {
            report.invalid(path, raw, "the allowed range is " + maxXpRange(), fallbackText);
            return fallback;
        }
        return (int) whole;
    }

    private static RoundingMode readRounding(ConfigurationSection section, ValidationReport report) {
        String path = LootingExpConfig.PATH_ROUNDING_MODE;
        RoundingMode fallback = LootingExpConfig.ROUNDING_DEFAULT;
        Object raw = section.get(path);
        if (raw == null) {
            report.missing(path, fallback.name());
            return fallback;
        }
        RoundingMode mode = raw instanceof String text ? RoundingMode.parse(text) : null;
        if (mode == null) {
            report.invalid(path, raw, "it must be one of " + roundingChoices(), fallback.name());
            return fallback;
        }
        return mode;
    }

    public static String tooManyDecimals() {
        return "values can have at most " + NumberParser.MAX_DECIMAL_PLACES + " decimal places";
    }

    public static String percentRange() {
        return Format.decimal(LootingExpConfig.PERCENT_MIN_HUNDREDTHS)
                + " - " + Format.decimal(LootingExpConfig.PERCENT_MAX_HUNDREDTHS);
    }

    public static String maxXpRange() {
        return Format.grouped(LootingExpConfig.MAX_XP_MIN)
                + " - " + Format.grouped(LootingExpConfig.MAX_XP_MAX);
    }

    public static String roundingChoices() {
        RoundingMode[] modes = RoundingMode.values();
        StringBuilder out = new StringBuilder(24);
        for (int i = 0; i < modes.length; i++) {
            if (i > 0) {
                out.append(i == modes.length - 1 ? " or " : ", ");
            }
            out.append(modes[i].name());
        }
        return out.toString();
    }

    private static void set(ConfigurationSection section,
                            ConfigurationSection bundledDefaults,
                            String path,
                            Object value) {
        boolean hadComments = !section.getComments(path).isEmpty();
        section.set(path, value);
        if (!hadComments && bundledDefaults != null) {
            List<String> comments = bundledDefaults.getComments(path);
            if (!comments.isEmpty()) {
                section.setComments(path, comments);
            }
        }
    }
}
