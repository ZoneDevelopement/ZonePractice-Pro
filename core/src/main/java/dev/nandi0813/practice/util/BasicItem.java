package dev.nandi0813.practice.util;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public class BasicItem {

    private final Material material;
    private final short damage;

    public BasicItem(final Material material, final short damage) {
        this.material = material;
        this.damage = damage;
    }

}
