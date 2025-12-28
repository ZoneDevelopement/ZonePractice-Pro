package dev.nandi0813.practice.Manager.GUI.Setup.Arena;

import dev.nandi0813.practice.Manager.Arena.ArenaManager;
import dev.nandi0813.practice.Manager.Arena.Arenas.Arena;
import dev.nandi0813.practice.Manager.Arena.Arenas.FFAArena;
import dev.nandi0813.practice.Manager.Arena.Arenas.Interface.DisplayArena;
import dev.nandi0813.practice.Manager.GUI.GUI;
import dev.nandi0813.practice.Manager.GUI.GUIManager;
import dev.nandi0813.practice.Manager.GUI.GUIType;
import dev.nandi0813.practice.Manager.GUI.Setup.Arena.ArenaSettings.Normal.ArenaMainGui;
import dev.nandi0813.practice.Manager.GUI.Setup.Arena.ArenaSettings.Normal.CopyGui;
import dev.nandi0813.practice.Manager.GUI.Setup.Arena.ArenaSettings.Normal.LadderSingleGui;
import dev.nandi0813.practice.Manager.GUI.Setup.Arena.ArenaSettings.Normal.LadderTypeGui;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ArenaSetupManager implements Listener {

    private static ArenaSetupManager instance;

    public static ArenaSetupManager getInstance() {
        if (instance == null)
            instance = new ArenaSetupManager();
        return instance;
    }

    private final Map<DisplayArena, Map<GUIType, GUI>> arenaSetupGUIs = new HashMap<>();

    public ArenaSetupManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public void buildArenaSetupGUIs(DisplayArena arena) {
        Map<GUIType, GUI> guis = new HashMap<>();

        if (arena instanceof FFAArena) {
            guis.put(GUIType.Arena_Main, new dev.nandi0813.practice.Manager.GUI.Setup.Arena.ArenaSettings.FFA.ArenaMainGui((FFAArena) arena));
            guis.put(GUIType.Arena_Ladders_Single, new dev.nandi0813.practice.Manager.GUI.Setup.Arena.ArenaSettings.FFA.LadderSingleGui((FFAArena) arena));
        } else {
            guis.put(GUIType.Arena_Main, new ArenaMainGui((Arena) arena));
            guis.put(GUIType.Arena_Ladders_Single, new LadderSingleGui((Arena) arena));
            guis.put(GUIType.Arena_Ladders_Type, new LadderTypeGui((Arena) arena));
            if (arena.isBuild())
                guis.put(GUIType.Arena_Copy, new CopyGui((Arena) arena));
        }

        arenaSetupGUIs.put(arena, guis);
    }

    public void loadGUIs() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            GUIManager.getInstance().addGUI(new ArenaSummaryGui());

            for (DisplayArena arena : ArenaManager.getInstance().getArenaList()) {
                buildArenaSetupGUIs(arena);
            }
        });
    }

    public void removeArenaGUIs(DisplayArena arena) {
        for (GUI gui : arenaSetupGUIs.get(arena).values()) {
            for (Player player : gui.getInGuiPlayers().keySet()) {
                GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).open(player);
            }
        }

        arenaSetupGUIs.remove(arena);
        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
    }

    @EventHandler
    public void onArenaCornerMarkerUse(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        Action action = e.getAction();
        ItemStack item = e.getItem();

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
                return;
        }

        if (!player.hasPermission("zpp.setup")) return;
        if (!action.equals(Action.LEFT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (item == null) return;

        if (!ArenaSetupUtil.getArenaMarkerList().containsKey(item)) return;
        DisplayArena arena = ArenaSetupUtil.getArenaMarkerList().get(item);
        if (arena == null) return;

        e.setCancelled(true);

        if (action.equals(Action.LEFT_CLICK_BLOCK))
            player.performCommand("arena set corner " + arena.getName() + " 1");
        else
            player.performCommand("arena set corner " + arena.getName() + " 2");
    }

}
