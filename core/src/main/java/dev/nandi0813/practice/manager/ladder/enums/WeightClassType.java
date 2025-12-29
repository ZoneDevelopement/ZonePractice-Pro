package dev.nandi0813.practice.manager.ladder.enums;

import lombok.Getter;

@Getter
public enum WeightClassType {

    UNRANKED("Unranked"),
    RANKED("Ranked"),
    UNRANKED_AND_RANKED("Unranked & Ranked");

    private final String name;

    WeightClassType(final String name) {
        this.name = name;
    }

}
