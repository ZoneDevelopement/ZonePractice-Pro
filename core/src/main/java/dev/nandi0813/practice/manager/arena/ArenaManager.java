package dev.nandi0813.practice.manager.arena;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.command.arena.arguments.CreateArg;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.StartUpCallback;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;

@Getter
public class ArenaManager implements Listener {

    private static ArenaManager instance;

    public static ArenaManager getInstance() {
        if (instance == null)
            instance = new ArenaManager();
        return instance;
    }

    public static final boolean LOAD_CHUNKS = ConfigManager.getBoolean("ARENA.LOAD-CHUNKS");
    public static final List<Chunk> LOADED_CHUNKS = new ArrayList<>();

    private final List<DisplayArena> arenaList = new ArrayList<>();
    private final Map<Cuboid, BasicArena> arenaCuboids = new HashMap<>();

    private final File folder = new File(ZonePractice.getInstance().getDataFolder() + "/arenas");

    private ArenaManager() {
    }

    public DisplayArena getArena(String arenaName) {
        for (DisplayArena arena : this.arenaList)
            if (arena.getName().equalsIgnoreCase(arenaName))
                return arena;
        return null;
    }

    public Arena getNormalArena(String arenaName) {
        for (Arena arena : this.getNormalArenas())
            if (arena.getName().equalsIgnoreCase(arenaName))
                return arena;
        return null;
    }

    public List<Arena> getNormalArenas() {
        List<Arena> arenas = new ArrayList<>();
        for (DisplayArena arena : this.arenaList)
            if (arena instanceof Arena)
                arenas.add((Arena) arena);
        return arenas;
    }

    public FFAArena getFFAArena(String arenaName) {
        for (FFAArena arena : this.getFFAArenas())
            if (arena.getName().equalsIgnoreCase(arenaName))
                return arena;
        return null;
    }

    public List<FFAArena> getFFAArenas() {
        List<FFAArena> arenas = new ArrayList<>();
        for (DisplayArena arena : this.arenaList)
            if (arena instanceof FFAArena)
                arenas.add((FFAArena) arena);
        return arenas;
    }

    public List<Arena> getEnabledArenas() {
        List<Arena> enabledArenas = new ArrayList<>();
        for (Arena arena : this.getNormalArenas())
            if (arena.isEnabled()) enabledArenas.add(arena);
        return enabledArenas;
    }

    public void loadArenas(final StartUpCallback boolCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            if (folder.isDirectory() && folder.listFiles() != null) {
                for (File arenaFile : Objects.requireNonNull(folder.listFiles())) {
                    if (arenaFile.isFile() && arenaFile.getName().endsWith(".yml")) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);

                        String name = config.getString("name");
                        if (name != null && CreateArg.checkName(name) == null) {
                            try {
                                ArenaType type = ArenaType.valueOf(config.getString("type"));

                                Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                                    if (type == ArenaType.FFA) {
                                        arenaList.add(new FFAArena(name));
                                    } else {
                                        arenaList.add(new Arena(name, type));
                                    }
                                });
                            } catch (IllegalArgumentException e) {
                                Common.sendConsoleMMMessage("<red>Can't load arena " + name + " because the type format isn't set correctly in the config.");
                            }
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), boolCallback::onLoadingDone);
        });
    }

    public void saveArenas() {
        for (DisplayArena arena : arenaList)
            arena.setData();
    }

    public void removeLadder(NormalLadder ladder) {
        for (DisplayArena arena : arenaList) {
            if (arena.getAssignedLadders().contains(ladder)) {
                // Remember this arena so we can restore the assignment when re-enabled
                ladder.getPreviouslyAssignedArenas().add(arena.getName());

                arena.getAssignedLadders().remove(ladder);

                ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arena).get(GUIType.Arena_Ladders_Single).update();
            }

            if (arena instanceof FFAArena) {
                FFA ffa = ((FFAArena) arena).getFfa();

                for (Map.Entry<Player, NormalLadder> ffaPlayer : ffa.getPlayers().entrySet()) {
                    if (ffaPlayer.getValue() == ladder) {
                        ffa.removePlayer(ffaPlayer.getKey());
                        Common.sendMMMessage(ffaPlayer.getKey(), LanguageManager.getString("FFA.LADDER-DISABLED-REMOVED"));
                    }
                }
            }
        }
    }

}
