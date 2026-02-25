package dev.nandi0813.practice.manager.gui.setup.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ServerHubGui extends GUI {

    private final ServerSaveGui serverSaveGui;
    private final ServerMatchesGui serverMatchesGui;
    private final ServerEventsGui serverEventsGui;
    private final ServerArmorLobbyGui serverArmorLobbyGui;

    public ServerHubGui() {
        super(GUIType.Server_Hub);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.SERVER.SERVER-MANAGER.TITLE"), 4));

        this.serverSaveGui = new ServerSaveGui(this);
        this.serverMatchesGui = new ServerMatchesGui(this);
        this.serverEventsGui = new ServerEventsGui(this);
        this.serverArmorLobbyGui = new ServerArmorLobbyGui(this);

        build();
    }

    @Override
    public void build() {
        Inventory inventory = gui.get(1);

        inventory.setItem(27, GUIFile.getGuiItem("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.BACK-TO").get());

        for (int i : new int[]{28, 29, 30, 31, 32, 33, 34, 35})
            inventory.setItem(i, GUIManager.getFILLER_ITEM());

        inventory.setItem(13, GUIFile.getGuiItem("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.SAVE").get());
        inventory.setItem(14, GUIFile.getGuiItem("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.LOBBY-ARMORS").get());

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            inventory.setItem(10, getInformationItem());
            inventory.setItem(15, getMatchesItem());
            inventory.setItem(16, getEventsItem());

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        Inventory inventory = e.getClickedInventory();
        ClickType click = e.getClick();

        e.setCancelled(true);

        if (inventory == null) return;

        if (inventory.getSize() > slot) {
            switch (slot) {
                case 27:
                    GUIManager.getInstance().searchGUI(GUIType.Setup_Hub).open(player);
                    break;
                case 13:
                    if (click.isLeftClick()) {
                        serverSaveGui.update();
                        serverSaveGui.open(player);
                    } else if (click.isRightClick()) {
                        if (PlayerCooldown.isActive(player, CooldownObject.SERVER_SETUP_SAVEALLDATA)) {
                            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.WAIT-BEFORE"));
                            return;
                        }

                        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
                                ServerManager.getInstance().getAutoSaveRunnable().save());

                        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.SERVER.SAVED-ALL-FILES"));

                        PlayerCooldown.addCooldown(player, CooldownObject.SERVER_SETUP_SAVEALLDATA, 15);
                    }
                    break;
                case 14:
                    serverArmorLobbyGui.open(player);
                    break;
                case 15:
                    serverMatchesGui.update();
                    serverMatchesGui.open(player);
                    break;
                case 16:
                    serverEventsGui.update();
                    serverEventsGui.open(player);
                    break;
            }
        }
    }

    private ItemStack getInformationItem() {
        return GUIFile.getGuiItem("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.INFORMATIONS")
                .replace("%onlinePlayers%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%onlineStaffs%", String.valueOf(PlayerUtil.getOnlineStaff().size()))
                .replace("%requiredDivision%", DivisionManager.getInstance().getMinimumForRanked() != null ? DivisionManager.getInstance().getMinimumForRanked().getFullName() : "&cN/A")
                .replace("%lobbyStatus%", ServerManager.getLobby() != null ? GUIFile.getString("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.INFORMATIONS.STATUS-NAMES.SET") : GUIFile.getString("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.INFORMATIONS.STATUS-NAMES.UNSET"))
                .replace("%enabledArena%", String.valueOf(ArenaManager.getInstance().getEnabledArenas().size()))
                .replace("%enabledLadder%", String.valueOf(LadderManager.getInstance().getEnabledLadders().size()))
                .replace("%enabledEvents%", String.valueOf(EventManager.getInstance().getEnabledEvents().size()))
                .get();
    }

    private ItemStack getMatchesItem() {
        return GUIFile.getGuiItem("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.MATCHES")
                .replace("%liveMatches%", String.valueOf(MatchManager.getInstance().getLiveMatches().size()))
                .get();
    }

    private ItemStack getEventsItem() {
        return GUIFile.getGuiItem("GUIS.SETUP.SERVER.SERVER-MANAGER.ICONS.EVENTS")
                .replace("%liveEvents%", String.valueOf(EventManager.getInstance().getEvents().size()))
                .get();
    }

}
