package com.xin.logRecord.constant;

public enum PrefixConstant {
    zero(0, ""),
    first(1, "【】"),
    second(2, "[]"),
    third(3, "()"),
    fourth(4, "<>");

    private final int number;
    private final String symbol;

    PrefixConstant(int number, String symbol) {
        this.number = number;
        this.symbol = symbol;
    }

    public static String getPrefixAndSuffix(int number) {
        PrefixConstant[] prefixConstant = PrefixConstant.values();
        for (PrefixConstant prefixAndSuffix : prefixConstant) {
            if (number == prefixAndSuffix.number) {
                return prefixAndSuffix.symbol;
            }
        }
        return null;
    }

}
