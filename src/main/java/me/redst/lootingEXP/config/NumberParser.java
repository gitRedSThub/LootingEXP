package me.redst.lootingEXP.config;

import java.math.BigDecimal;

public final class NumberParser {

    public static final int MAX_DECIMAL_PLACES = 2;

    public enum Status {
        OK,
        NOT_A_NUMBER,
        TOO_MANY_DECIMALS
    }

    public record Result(Status status, long hundredths) {

        public boolean ok() {
            return this.status == Status.OK;
        }
    }

    private static final Result NOT_A_NUMBER = new Result(Status.NOT_A_NUMBER, 0L);
    private static final Result TOO_MANY_DECIMALS = new Result(Status.TOO_MANY_DECIMALS, 0L);

    private static final int MIN_SCALE = -9;

    private NumberParser() {
    }

    public static Result parse(String text) {
        if (text == null) {
            return NOT_A_NUMBER;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return NOT_A_NUMBER;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(trimmed);
        } catch (NumberFormatException ex) {
            return NOT_A_NUMBER;
        }
        return fromDecimal(value);
    }

    public static Result parse(Object raw) {
        if (raw instanceof Double || raw instanceof Float) {
            return fromDecimal(BigDecimal.valueOf(((Number) raw).doubleValue()));
        }
        if (raw instanceof Number number) {
            return fromDecimal(new BigDecimal(number.toString()));
        }
        if (raw instanceof String text) {
            return parse(text);
        }
        return NOT_A_NUMBER;
    }

    private static Result fromDecimal(BigDecimal value) {
        int scale = value.scale();
        if (scale > MAX_DECIMAL_PLACES) {
            return TOO_MANY_DECIMALS;
        }
        if (scale < MIN_SCALE) {
            return NOT_A_NUMBER;
        }
        try {
            long hundredths = value.setScale(MAX_DECIMAL_PLACES, java.math.RoundingMode.UNNECESSARY)
                    .unscaledValue()
                    .longValueExact();
            return new Result(Status.OK, hundredths);
        } catch (ArithmeticException ex) {
            return NOT_A_NUMBER;
        }
    }
}
