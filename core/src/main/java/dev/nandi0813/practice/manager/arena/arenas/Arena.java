package dev.nandi0813.practice.manager.arena.arenas;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.ArenaType;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class Arena extends DisplayArena {

    private final List<ArenaCopy> copies = new ArrayList<>();
    @Setter
    private boolean copying = false;

    @Setter
    private Set<LadderType> assignedLadderTypes = new HashSet<>();
    @Setter
    private boolean allowCustomKitOnMap = true;

    @Setter
    private boolean frozen; // If the arena is frozen players can't start a new game with it.
    @Setter
    private int sideBuildLimit = 0;

    public Arena(String name, ArenaType type) {
        super(name, type);

        this.build = type.isBuild();
        this.available = true;
        this.createCuboid();

        if (build) {
            loadCopies();
        }

        this.getData();
    }

    @Override
    public void setData() {
        YamlConfiguration config = arenaFile.getConfig();

        config.set("copies", null);
        for (ArenaCopy arenaCopy : copies)
            arenaCopy.setBasicData(config, "copies." + arenaCopy.getName() + ".");

        config.set("name", this.name);
        config.set("type", this.type.toString());
        config.set("enabled", this.enabled);
        config.set("ladderTypes", ArenaUtil.getLadderTypeNames(this));
        config.set("ladders", ArenaUtil.getLadderNames(this));
        config.set("allowCustomKitOnMap", this.allowCustomKitOnMap);

        if (this.getIcon() != null)
            config.set("icon", this.getIcon());

        config.set("sideBuildLimit", this.getSideBuildLimit());

        super.setBasicData(config, "");

        arenaFile.saveFile();
    }

    @Override
    public void getData() {
        YamlConfiguration config = arenaFile.getConfig();

        if (config.isItemStack("icon"))
            this.setIcon(config.getItemStack("icon"));

        if (config.isInt("sideBuildLimit")) {
            this.setSideBuildLimit(config.getInt("sideBuildLimit"));

            if (this.getSideBuildLimit() < 0 || this.getSideBuildLimit() > 10)
                this.setSideBuildLimit(0);
        }

        if (config.isBoolean("enabled"))
            this.setEnabled(config.getBoolean("enabled"));

        if (config.isList("ladderTypes")) {
            for (String ladderTypeName : config.getStringList("ladderTypes")) {
                assignedLadderTypes.add(LadderType.valueOf(ladderTypeName));
            }
        }

        if (config.isList("ladders")) {
            for (String ladderName : config.getStringList("ladders")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);

                if (ladder != null && ladder.isEnabled() && this.getAssignedLadderTypes().contains(ladder.getType()))
                    assignedLadders.add(ladder);
            }
        }

        if (config.isBoolean("allowCustomKitOnMap")) {
            this.setAllowCustomKitOnMap(config.getBoolean("allowCustomKitOnMap"));
        }

        super.getBasicData(config, "");

        if (this.isEnabled() && !this.isReadyToEnable())
            this.setEnabled(false);
    }

    private void loadCopies() {
        if (!arenaFile.getConfig().isConfigurationSection("copies")) {
            return;
        }

        for (String copyName : arenaFile.getConfig().getConfigurationSection("copies").getKeys(false)) {
            ArenaCopy arenaCopy = new ArenaCopy(copyName, this);
            arenaCopy.getBasicData(arenaFile.getConfig(), "copies." + copyName + ".");
            copies.add(arenaCopy);
        }
    }

    public void deleteLastCopy(boolean updateGUIs) {
        if (!build) return;
        if (copies.isEmpty()) return;

        ArenaCopy arenaCopy = copies.get(copies.size() - 1);
        arenaCopy.delete();
        copies.remove(arenaCopy);
        ArenaManager.getInstance().getArenaCuboids().remove(cuboid);

        if (updateGUIs) {
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(this).get(GUIType.Arena_Copy).update();
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(this).get(GUIType.Arena_Main).update();
            GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
        }
    }

    public void deleteCopies() {
        if (!build) return;

        for (int i = 0; i < copies.size(); i++)
            deleteLastCopy(i == copies.size() - 1);

        arenaFile.saveFile();
    }

    @Override
    public boolean deleteData() {
        deleteCopies();

        ArenaManager.getInstance().getArenaCuboids().remove(cuboid);
        return arenaFile.getFile().delete();
    }

    public NormalArena getAvailableArena() {
        if (!enabled || frozen) return null;
        if (available) return this;

        if (build) {
            for (ArenaCopy arenaCopy : copies) {
                if (arenaCopy.isAvailable()) {
                    return arenaCopy;
                }
            }
        }

        return null;
    }

    public boolean hasCopies() {
        return build && !copies.isEmpty();
    }

    @Override
    public boolean isReadyToEnable() {
        if (this.getIcon() != null && cuboid != null && position1 != null && position2 != null && !assignedLadderTypes.isEmpty()) {
            if (assignedLadderTypes.contains(LadderType.BEDWARS) || assignedLadderTypes.contains(LadderType.FIREBALL_FIGHT))
                return bedLoc1 != null && bedLoc2 != null;
            if (assignedLadderTypes.contains(LadderType.BRIDGES) || assignedLadderTypes.contains(LadderType.BATTLE_RUSH))
                return portalLoc1 != null && portalLoc2 != null;
            return true;
        } else
            return false;
    }

    @Override
    public Set<NormalLadder> getAssignableLadders() {
        Set<NormalLadder> list = new HashSet<>();

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (this.build == ladder.isBuild()) {
                list.add(ladder);
            }
        }

        return list;
    }

}
