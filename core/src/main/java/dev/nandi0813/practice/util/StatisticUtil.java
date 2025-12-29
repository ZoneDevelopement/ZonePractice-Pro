package dev.nandi0813.practice.util;

public enum StatisticUtil {
    ;

    public static String getProgressBar(final double progress) {
        int numberOfColoredBars = (int) Math.floor(progress / 10.0);
        int numberOfEmptyBars = 10 - numberOfColoredBars;

        String progressBar = "&l";
        for (int i = 0; i < numberOfColoredBars; i++)
            progressBar = progressBar.concat("┃");

        if (numberOfEmptyBars > 0) {
            progressBar = progressBar.concat("&7&l");
            for (int i = 0; i < numberOfEmptyBars; i++)
                progressBar = progressBar.concat("┃");
        }

        return progressBar;
    }

}
