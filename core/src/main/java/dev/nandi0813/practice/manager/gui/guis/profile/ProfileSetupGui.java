package dev.nandi0813.practice.manager.gui.guis.profile;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.RankedBan;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ProfileSetupGui extends GUI {

    private final Profile profile;
    private final ProfileLadderStats profileLadderStats;

    public ProfileSetupGui(Profile profile) {
        super(GUIType.Profile_Setup);
        this.profile = profile;
        this.profileLadderStats = new ProfileLadderStats(profile, this);

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PLAYER-INFORMATION.MAIN-PAGE.TITLE").replace("%player%", profile.getPlayer().getName()), 4));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);

            for (int i : new int[]{27, 28, 29, 30, 31, 32, 33, 34, 35})
                inventory.setItem(i, GUIManager.getFILLER_ITEM());

            inventory.setItem(10, getBasicInfoItem(profile));
            inventory.setItem(11, getOnlineItem(profile));
            inventory.setItem(13, getPartyItem(profile));
            inventory.setItem(14, getGameItem(profile));
            inventory.setItem(16, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.STATISTICS").get());
            inventory.setItem(19, getRankedBanItem(profile));
            inventory.setItem(31, GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.REFRESH").get());

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        Inventory inventory = e.getView().getTopInventory();
        ItemStack item = e.getCurrentItem();
        ClickType click = e.getClick();

        e.setCancelled(true);

        if (inventory.getSize() > slot && item != null) {
            switch (slot) {
                case 14:
                    Player target = profile.getPlayer().getPlayer();
                    if (target == null) return;

                    ProfileStatus profileStatus = profile.getStatus();
                    switch (profileStatus) {
                        case MATCH:
                            Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
                            if (match == null) return;

                            if (click.isLeftClick()) {
                                if (!player.hasPermission("zpp.practice.info.teleport")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                match.addSpectator(player, target, true, false);
                            } else if (click.isRightClick()) {
                                if (!player.hasPermission("zpp.practice.info.cancel")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                Common.sendMMMessage(player, LanguageManager.getString("PROFILE.PLAYER-REMOVED-MATCH").replaceAll("%target%", target.getName()));
                                match.sendMessage(LanguageManager.getString("PROFILE.REMOVED-PLAYER-MATCH").replaceAll("%target%", target.getName()), true);

                                match.removePlayer(target, false);
                            }
                            break;
                        case FFA:
                            FFA ffa = FFAManager.getInstance().getFFAByPlayer(target);
                            if (ffa == null) return;

                            if (click.isLeftClick()) {
                                if (!player.hasPermission("zpp.practice.info.teleport")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                ffa.addSpectator(player, target, true, false);
                            } else if (click.isRightClick()) {
                                if (!player.hasPermission("zpp.practice.info.cancel")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                ffa.removePlayer(target);
                            }
                            break;
                        case EVENT:
                            Event event = EventManager.getInstance().getEventByPlayer(target);
                            if (event == null) return;

                            if (click.isLeftClick()) {
                                if (!player.hasPermission("zpp.practice.info.teleport")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                event.addSpectator(player, target, true, false);
                            } else if (click.isRightClick()) {
                                if (!player.hasPermission("zpp.practice.info.cancel")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                Common.sendMMMessage(player, LanguageManager.getString("PROFILE.PLAYER-REMOVED-EVENT").replaceAll("%target%", target.getName()));
                                event.sendMessage(LanguageManager.getString("PROFILE.REMOVED-PLAYER-EVENT").replaceAll("%target%", target.getName()), true);

                                event.removePlayer(target, false);
                            }
                            break;
                        case SPECTATE:
                            Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(target);

                            if (click.isLeftClick()) {
                                if (!player.hasPermission("zpp.practice.info.teleport")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                if (spectatable != null) {
                                    spectatable.addSpectator(player, target, true, false);
                                }
                            } else if (click.isRightClick()) {
                                if (!player.hasPermission("zpp.practice.info.cancel")) {
                                    Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                                    return;
                                }

                                if (spectatable != null) {
                                    spectatable.removeSpectator(target);
                                }

                                Common.sendMMMessage(player, LanguageManager.getString("PROFILE.PLAYER-REMOVED-SPECTATOR").replaceAll("%target%", target.getName()));
                            }
                            break;
                    }
                    break;
                case 16:
                    profileLadderStats.update();
                    profileLadderStats.open(player);
                    break;
                case 19:
                    player.performCommand("practice ranked unban " + profile.getPlayer().getName());
                    this.update();
                    break;
                case 31:
                    this.update();
                    break;
            }
        }
    }

    private static ItemStack getBasicInfoItem(Profile profile) {
        GUIItem guiItem = new GUIItem(ClassImport.getClasses().getItemMaterialUtil().getPlayerHead(profile.getPlayer()));
        guiItem.setName(GUIFile.getString("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.BASIC-INFO.NAME"));
        guiItem.setLore(GUIFile.getStringList("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.BASIC-INFO.LORE"));

        guiItem
                .replaceAll("%player%", profile.getPlayer().getName())
                .replaceAll("%uuid%", String.valueOf(profile.getUuid()))
                .replaceAll("%first_played%", StringUtil.getDate(profile.getFirstJoin()))
                .replaceAll("%last_played%", (profile.getStatus().equals(ProfileStatus.OFFLINE) ? StringUtil.getDate(profile.getLastJoin()) : GUIFile.getString("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.BASIC-INFO.ONLINE-STATUS")))
                .replaceAll("%unranked_left%", String.valueOf(profile.getUnrankedLeft()))
                .replaceAll("%ranked_left%", String.valueOf(profile.getRankedLeft()))
                .replaceAll("%division_fullName%", profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "&cN/A")
                .replaceAll("%division_shortName%", profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "&cN/A");

        return guiItem.get();
    }

    private static ItemStack getOnlineItem(Profile profile) {
        Player player = profile.getPlayer().getPlayer();

        if (profile.getStatus().equals(ProfileStatus.OFFLINE) || player == null)
            return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.PLAYER-OFFLINE").get();
        else
            return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.PLAYER-ONLINE")
                    .replaceAll("%world%", player.getWorld().getName())
                    .replaceAll("%gamemode%", player.getGameMode().name())
                    .replaceAll("%flying%", String.valueOf(player.isFlying()))
                    .replaceAll("%tablist_name%", player.getPlayerListName())
                    .replaceAll("%health%", String.valueOf(player.getHealth()))
                    .replaceAll("%food%", String.valueOf(player.getFoodLevel()))
                    .replaceAll("%hit_delay%", String.valueOf(player.getMaximumNoDamageTicks()))
                    .replaceAll("%ping%", String.valueOf(ClassImport.getClasses().getPlayerUtil().getPing(player)))
                    .get();
    }

    private static ItemStack getGameItem(Profile profile) {
        ProfileStatus profileStatus = profile.getStatus();

        switch (profileStatus) {
            case OFFLINE:
                return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.GAME.OFFLINE").get();
            case MATCH:
                return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.GAME.IN-MATCH").get();
            case FFA:
                return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.GAME.IN-FFA").get();
            case EVENT:
                return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.GAME.IN-EVENT").get();
            case SPECTATE:
                return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.GAME.SPECTATING").get();
            default:
                return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.ONLINE-INFO.GAME.NOTHING").get();
        }
    }

    private static ItemStack getPartyItem(Profile profile) {
        if (profile.isParty())
            return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.PARTY.IN-PARTY").get();
        else
            return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.PARTY.NOT-IN-PARTY").get();
    }

    private static ItemStack getRankedBanItem(Profile profile) {
        if (profile.getRankedBan().isBanned()) {
            RankedBan rankedBan = profile.getRankedBan();
            return GUIFile.getGuiItem("GUIS.PLAYER-INFORMATION.MAIN-PAGE.ICONS.RANKED-BAN")
                    .replaceAll("%player%", profile.getPlayer().getName())
                    .replaceAll("%banner%", rankedBan.getBanner() == null ? "&cConsole" : rankedBan.getBanner().getPlayer().getName())
                    .replaceAll("%reason%", rankedBan.getReason() == null ? "&cN/A" : rankedBan.getReason())
                    .replaceAll("%time%", StringUtil.getDate(rankedBan.getTime()))
                    .get();
        } else
            return null;
    }

}
