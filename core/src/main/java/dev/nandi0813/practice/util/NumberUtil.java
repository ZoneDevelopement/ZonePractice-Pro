package dev.nandi0813.practice.util;

import java.text.DecimalFormat;

public enum NumberUtil {
    ;

    public static double roundDouble(double value) {
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.parseDouble(df.format(value).replace(",", "."));
    }

    public static int doubleToInt(double value) {
        return (int) value;
    }

    public static int getRandomNumber(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

}
