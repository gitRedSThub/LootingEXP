package me.redst.lootingEXP.util;

import java.util.Locale;

public final class Format {

    private Format() {
    }

    public static String decimal(long hundredths) {
        long magnitude = Math.abs(hundredths);
        StringBuilder out = new StringBuilder(16);
        if (hundredths < 0L) {
            out.append('-');
        }
        out.append(magnitude / 100L).append('.');
        long fraction = magnitude % 100L;
        if (fraction < 10L) {
            out.append('0');
        }
        return out.append(fraction).toString();
    }

    public static String grouped(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
