package me.redst.lootingEXP.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ValidationReport {

    private final List<String> warnings = new ArrayList<>(0);
    private int invalidValues;
    private int missingValues;
    private boolean unreadable;

    public void unreadable() {
        this.unreadable = true;
    }

    public boolean isUnreadable() {
        return this.unreadable;
    }

    public void invalid(String path, Object found, String problem, String used) {
        this.invalidValues++;
        this.warnings.add("'" + path + "' was set to " + describe(found) + " but " + problem
                + ". The default (" + used + ") has been restored.");
    }

    public void missing(String path, String used) {
        this.missingValues++;
        this.warnings.add("'" + path + "' was missing and has been added with its default (" + used + ").");
    }

    public int invalidValues() {
        return this.invalidValues;
    }

    public boolean needsRewrite() {
        return this.invalidValues > 0 || this.missingValues > 0;
    }

    public void logTo(Logger logger) {
        for (String warning : this.warnings) {
            logger.warning(warning);
        }
    }

    private static String describe(Object found) {
        if (found == null) {
            return "nothing";
        }
        String text = String.valueOf(found);
        return text.isEmpty() ? "an empty value" : text;
    }
}
