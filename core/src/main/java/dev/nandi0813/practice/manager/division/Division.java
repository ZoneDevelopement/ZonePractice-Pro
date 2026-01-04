package dev.nandi0813.practice.manager.division;

import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class Division {

    private final String configName;

    private final String fullName;
    private final String shortName;
    private final String color;

    private final Material iconMaterial;
    private short iconDamage;

    private final int experience;
    private final int win;
    private final int elo;

    public Division(final String configName, final ConfigurationSection section) {
        this.configName = configName;

        this.color = section.getString("DATA.COLOR");
        this.fullName = section.getString("DATA.NAME.FULL").replace("%color%", color);
        this.shortName = section.getString("DATA.NAME.SHORT").replace("%color%", color);

        this.iconMaterial = Material.valueOf(section.getString("DATA.ICON-MATERIAL"));

        if (section.isInt("DATA.ICON-DAMAGE")) {
            try {
                this.iconDamage = Short.parseShort(String.valueOf(section.getInt("DATA.ICON-DAMAGE")));
            } catch (NumberFormatException ignored) {
            }
        }

        this.experience = section.getInt("REQUIREMENTS.EXPERIENCE");
        this.win = section.getInt("REQUIREMENTS.WINS");
        this.elo = section.getInt("REQUIREMENTS.ELO");
    }

    public boolean isValid() {
        return fullName != null && shortName != null && color != null && iconMaterial != null && experience >= 0 && win >= 0;
    }

    public Component getComponentFullName() {
        return ZonePractice.getMiniMessage().deserialize(fullName);
    }

    public Component getComponentShortName() {
        return ZonePractice.getMiniMessage().deserialize(shortName);
    }

}
