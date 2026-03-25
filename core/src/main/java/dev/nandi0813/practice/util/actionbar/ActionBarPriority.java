package dev.nandi0813.practice.util.actionbar;

import lombok.Getter;

@Getter
public enum ActionBarPriority {
    LOW(0),
    NORMAL(1),
    HIGH(2),
    HIGHEST(3);

    private final int weight;

    ActionBarPriority(int weight) {
        this.weight = weight;
    }
}