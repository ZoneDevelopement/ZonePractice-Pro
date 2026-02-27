package dev.nandi0813.practice.manager.arena.arenas;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.ArenaType;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

@Getter
public class FFAArena extends DisplayArena {

    private final FFA ffa;
    private boolean reKitAfterKill;
    private boolean lobbyAfterDeath;

    public FFAArena(String name) {
        super(name, ArenaType.FFA);

        this.bedLoc1 = null;
        this.bedLoc2 = null;
        this.portalLoc1 = null;
        this.portalLoc2 = null;
        this.portalProtection = false;

        this.getData();

        this.ffa = new FFA(this);
        this.ffa.open();
    }

    @Override
    public void setData() {
        YamlConfiguration config = arenaFile.getConfig();

        config.set("name", this.name);
        config.set("type", this.type.toString());
        config.set("enabled", this.enabled);

        if (this.getIcon() != null)
            config.set("icon", this.getIcon());

        config.set("build", this.build);
        config.set("reKitAfterKill", this.reKitAfterKill);
        config.set("lobbyAfterDeath", this.lobbyAfterDeath);

        config.set("ladders", ArenaUtil.getLadderNames(this));

        super.setBasicData(config, "");

        arenaFile.saveFile();
    }

    @Override
    public void getData() {
        YamlConfiguration config = arenaFile.getConfig();

        if (config.isItemStack("icon"))
            this.setIcon(config.getItemStack("icon"));

        if (config.isBoolean("build"))
            this.setBuild(config.getBoolean("build"));

        if (config.isBoolean("reKitAfterKill"))
            this.setReKitAfterKill(config.getBoolean("reKitAfterKill"));

        if (config.isBoolean("lobbyAfterDeath"))
            this.setLobbyAfterDeath(config.getBoolean("lobbyAfterDeath"));

        if (config.isList("ladders")) {
            for (String ladderName : config.getStringList("ladders")) {
                NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);

                if (ladder != null && ladder.isEnabled())
                    assignedLadders.add(ladder);
            }
        }

        super.getBasicData(config, "");

        if (config.isBoolean("enabled"))
            this.setEnabled(config.getBoolean("enabled"));

        if (this.isEnabled() && !this.isReadyToEnable())
            this.setEnabled(false);
    }

    @Override
    public boolean isReadyToEnable() {
        return this.getIcon() != null && cuboid != null && !ffaPositions.isEmpty() && !assignedLadders.isEmpty();
    }

    @Override
    public Set<NormalLadder> getAssignableLadders() {
        Set<NormalLadder> list = new HashSet<>();

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (this.build && ladder.getType().equals(LadderType.BUILD) && ladder.getLadderKnockback().isDefault())
                list.add(ladder);
            else if (!this.build && ladder.getType().equals(LadderType.BASIC) && ladder.getLadderKnockback().isDefault())
                list.add(ladder);
        }

        return list;
    }

    @Override
    public boolean deleteData() {
        ArenaManager.getInstance().getArenaCuboids().remove(cuboid);
        return arenaFile.getFile().delete();
    }

    public void setReKitAfterKill(boolean reKitAfterKill) throws IllegalStateException {
        if (this.enabled) {
            throw new IllegalStateException("Cannot edit while arena is enabled.");
        }

        this.reKitAfterKill = reKitAfterKill;
    }

    public void setLobbyAfterDeath(boolean lobbyAfterDeath) throws IllegalStateException {
        if (this.enabled) {
            throw new IllegalStateException("Cannot edit while arena is enabled.");
        }

        this.lobbyAfterDeath = lobbyAfterDeath;
    }

    public void setBuild(boolean build) {
        if (this.enabled) {
            throw new IllegalStateException("Cannot edit while arena is enabled.");
        }

        this.build = build;

        Set<NormalLadder> assignableLadders = this.getAssignableLadders();
        this.assignedLadders.removeIf(ladder -> !assignableLadders.contains(ladder));

        GUI ladderGUI = ArenaGUISetupManager.getInstance().getArenaSetupGUIs().getOrDefault(this, new HashMap<>()).get(GUIType.Arena_Ladders_Single);
        if (ladderGUI != null) {
            ladderGUI.update();
        }
    }

}
