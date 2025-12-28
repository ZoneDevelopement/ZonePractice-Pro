package dev.nandi0813.practice.Manager.PlayerDisplay.Nametag;

import dev.nandi0813.practice.Module.Util.VersionChecker;
import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class FakeTeam {

    @Getter
    private static final List<String> createdTeamsNames = new ArrayList<>();

    private static final String UNIQUE_ID = NametagManager.generateUUID();
    private static int ID = 0;
    private final ArrayList<String> members = new ArrayList<>();
    private String name;
    private Component prefix;
    private NamedTextColor nameColor;
    private Component suffix;
    private boolean visible = true;

    public FakeTeam(Component prefix, NamedTextColor nameColor, Component suffix, int sortPriority) {
        ++ID;
        String generatedName = UNIQUE_ID + "_" + getNameFromInput(sortPriority) + ID;
        while (createdTeamsNames.contains(generatedName)) {
            ++ID;
            generatedName = NametagManager.generateUUID() + "_" + getNameFromInput(sortPriority) + ID;
        }
        this.name = generatedName;

        if (Objects.equals(VersionChecker.getBukkitVersion(), VersionChecker.BukkitVersion.v1_8_R3)) {
            this.name = this.name.length() > 16 ? this.name.substring(0, 16) : this.name;
        } else {
            this.name = this.name.length() > 256 ? this.name.substring(0, 256) : this.name;
        }

        this.prefix = prefix;
        this.nameColor = nameColor;
        this.suffix = suffix;

        createdTeamsNames.add(this.name);
    }

    public void addMember(String player) {
        if (!members.contains(player)) {
            members.add(player);
        }
    }

    public boolean isSimilar(Component prefix, NamedTextColor nameColor, Component suffix) {
        return this.prefix.equals(prefix) && this.nameColor == nameColor && this.suffix.equals(suffix);
    }

    /**
     * This is a special method to sort nametags in
     * the tablist. It takes a priority and converts
     * it to an alphabetic representation to force a
     * specific sort.
     *
     * @param input the sort priority
     * @return the team name
     */
    private String getNameFromInput(int input) {
        if (input < 0) return "Z";
        char letter = (char) ((input / 5) + 65);
        int repeat = input % 5 + 1;
        return String.valueOf(letter).repeat(repeat);
    }

}