package dev.nandi0813.practice.manager.ladder.enums;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import lombok.Getter;

public enum KnockbackType {

    DEFAULT
            (
                    0,
                    0,
                    0,
                    0
            ),
    NORMAL
            (
                    ConfigManager.getDouble("KNOCKBACK.NORMAL.AIR-HORIZONTAL"),
                    ConfigManager.getDouble("KNOCKBACK.NORMAL.AIR-VERTICAL"),
                    ConfigManager.getDouble("KNOCKBACK.NORMAL.HORIZONTAL"),
                    ConfigManager.getDouble("KNOCKBACK.NORMAL.VERTICAL")
            ),
    COMBO
            (
                    ConfigManager.getDouble("KNOCKBACK.COMBO.AIR-HORIZONTAL"),
                    ConfigManager.getDouble("KNOCKBACK.COMBO.AIR-VERTICAL"),
                    ConfigManager.getDouble("KNOCKBACK.COMBO.HORIZONTAL"),
                    ConfigManager.getDouble("KNOCKBACK.COMBO.VERTICAL")
            );

    @Getter
    private final double airhorizontal;
    @Getter
    private final double airvertical;
    @Getter
    private final double horizontal;
    @Getter
    private final double vertical;

    KnockbackType(double airhorizontal, double airvertical, double horizontal, double vertical) {
        this.airhorizontal = airhorizontal;
        this.airvertical = airvertical;
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

}
