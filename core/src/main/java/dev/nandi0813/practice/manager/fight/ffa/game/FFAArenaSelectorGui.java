package dev.nandi0813.practice.manager.fight.ffa.game;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FFAArenaSelectorGui extends GUI {

    private static final String BUILD_ON = GUIFile.getString("GUIS.FFA.ARENA-SELECTOR.ICONS.ARENA.BUILD-STATUS.ENABLED");
    private static final String BUILD_OFF = GUIFile.getString("GUIS.FFA.ARENA-SELECTOR.ICONS.ARENA.BUILD-STATUS.DISABLED");
    private static final GUIItem ARENA_ITEM = GUIFile.getGuiItem("GUIS.FFA.ARENA-SELECTOR.ICONS.ARENA");
    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.FFA.ARENA-SELECTOR.ICONS.FILLER").get();

    private final Map<Integer, FFAArena> arenaSlots = new HashMap<>();

    public FFAArenaSelectorGui() {
        super(GUIType.FFA_Arena_Selector);

        int rows = Math.max(1, GUIFile.getInt("GUIS.FFA.ARENA-SELECTOR.ROWS"));
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.FFA.ARENA-SELECTOR.TITLE"), rows));
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);
        inventory.clear();
        arenaSlots.clear();

        List<FFAArena> openArenas = ArenaManager.getInstance().getFFAArenas().stream()
                .filter(arena -> arena.isEnabled() && arena.getFfa() != null && arena.getFfa().isOpen())
                .toList();

        for (FFAArena arena : openArenas) {
            FFA ffa = arena.getFfa();

            GUIItem guiItem = ARENA_ITEM.cloneItem()
                    .replace("%arena%", arena.getDisplayName())
                    .replace("%players%", String.valueOf(ffa.getPlayers().size()))
                    .replace("%build_status%", arena.isBuild() ? BUILD_ON : BUILD_OFF)
                    .replace("%rekit_after_kill%", arena.isReKitAfterKill() ? BUILD_ON : BUILD_OFF)
                    .replace("%health_reset_on_kill%", arena.isHealthResetOnKill() ? BUILD_ON : BUILD_OFF)
                    .replace("%lobby_after_death%", arena.isLobbyAfterDeath() ? BUILD_ON : BUILD_OFF)
                    .replace("%ladders%", String.valueOf(arena.getAssignedLadders().size()));

            if (arena.getIcon() != null) {
                guiItem.setBaseItem(arena.getIcon());
            }

            int slot = inventory.firstEmpty();
            if (slot == -1) break;

            inventory.setItem(slot, guiItem.get());
            arenaSlots.put(slot, arena);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_ITEM);
            }
        }

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (!arenaSlots.containsKey(slot)) return;

        FFAArena arena = arenaSlots.get(slot);
        if (arena == null) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
            player.closeInventory();
            return;
        }

        FFA ffa = arena.getFfa();
        if (ffa == null || !ffa.isOpen()) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.COMMAND.JOIN.ARENA-CLOSED").replace("%arena%", arena.getDisplayName()));
            player.closeInventory();
            return;
        }

        // If the arena has only one ladder, join directly
        if (arena.getAssignedLadders().size() == 1) {
            NormalLadder ladder = arena.getAssignedLadders().iterator().next();
            player.closeInventory();
            ffa.addPlayer(player, ladder);
        } else {
            // Open the ladder selector for this arena
            ffa.getLadderSelectorGui().update();
            ffa.getLadderSelectorGui().open(player);
        }
    }

}

