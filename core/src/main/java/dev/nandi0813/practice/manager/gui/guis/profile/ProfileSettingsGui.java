package dev.nandi0813.practice.manager.gui.guis.profile;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileWorldTime;
import dev.nandi0813.practice.manager.sidebar.SidebarManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class ProfileSettingsGui extends GUI {

    private final Profile profile;

    public ProfileSettingsGui(final Profile profile) {
        super(GUIType.Profile_Settings);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PLAYER-SETTINGS.TITLE").replace("%player%", profile.getPlayer().getName()), 4));
        this.profile = profile;

        this.build();
    }

    @Override
    public void build() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            Inventory inventory = gui.get(1);
            ItemStack fillerItem = GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.FILLER-ITEM").get();

            for (int i = 0; i < inventory.getSize(); i++)
                inventory.setItem(i, fillerItem);

            update();
        });
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        inventory.setItem(10, getDuelRequestItem(profile.isDuelRequest()));
        inventory.setItem(11, getSidebarItem(profile.isSidebar()));
        inventory.setItem(12, getPartyInviteItem(profile.isPartyInvites()));
        inventory.setItem(13, getPrivateMessageItem(profile.isPrivateMessages()));
        inventory.setItem(14, getHidePlayersItem(profile.isHidePlayers()));
        inventory.setItem(15, getAllowSpectatorsItem(profile.isAllowSpectate()));
        inventory.setItem(16, GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.WORLD-TIME")
                .replaceAll("%worldTime%", profile.getWorldTime().getName())
                .get());
        inventory.setItem(22, getFlyingItem(profile.isFlying()));

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        ItemStack item = e.getCurrentItem();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (profile == null) return;
        if (item == null) return;
        if (item.equals(GUIManager.getFILLER_ITEM())) return;

        if (e.getView().getTopInventory().getSize() > slot) {
            if (profile != this.profile) {
                Common.sendMMMessage(player, LanguageManager.getString("PROFILE.CANT-CHANGE-TARGET-SETTINGS").replaceAll("%target%", this.getProfile().getPlayer().getName()));
                return;
            }

            if (PlayerCooldown.isActive(player, CooldownObject.PLAYER_SETTINGS)) {
                Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("PROFILE.TOGGLE-COOLDOWN"), PlayerCooldown.getLeftInDouble(player, CooldownObject.PLAYER_SETTINGS)));
                return;
            }

            switch (slot) {
                case 10:
                    if (player.hasPermission("zpp.settings.duelrequest")) {
                        profile.setDuelRequest(!profile.isDuelRequest());

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));

                    break;
                case 11:
                    if (player.hasPermission("zpp.settings.scoreboard")) {
                        if (profile.isSidebar())
                            SidebarManager.getInstance().unLoadSidebar(player);
                        else
                            SidebarManager.getInstance().loadSidebar(player);

                        profile.setSidebar(!profile.isSidebar());

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));

                    break;
                case 12:
                    if (player.hasPermission("zpp.settings.partyinvite")) {
                        profile.setPartyInvites(!profile.isPartyInvites());

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));

                    break;
                case 13:
                    if (player.hasPermission("zpp.settings.privatemessage")) {
                        if (player.hasPermission("zpp.settings.privatemessage")) {
                            profile.setPrivateMessages(!profile.isPrivateMessages());

                            update();
                            if (!player.hasPermission("zpp.bypass.cooldown"))
                                PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                        } else
                            Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.PRIVATE-MSG-OFF"));

                    break;
                case 14:
                    if (player.hasPermission("zpp.settings.playerhide")) {
                        profile.setHidePlayers(!profile.isHidePlayers());
                        PlayerHider.getInstance().toggleLobbyVisibility(player);

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));

                    break;
                case 15:
                    if (player.hasPermission("zpp.settings.allowspectate")) {
                        profile.setAllowSpectate(!profile.isAllowSpectate());

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));

                    break;
                case 16:
                    if (player.hasPermission("zpp.settings.worldtime")) {
                        ProfileWorldTime newTime = ProfileWorldTime.getNextWorldTime(profile.getWorldTime());
                        profile.setWorldTime(newTime);
                        PlayerUtil.setPlayerWorldTime(player);

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));

                    break;
                case 22:
                    if (player.hasPermission("zpp.settings.fly")) {
                        profile.setFlying(!profile.isFlying());

                        if (profile.isFlying()) {
                            player.setAllowFlight(true);
                            player.setFlying(true);
                        } else {
                            player.setFlying(false);
                            player.setAllowFlight(false);
                        }

                        update();
                        if (!player.hasPermission("zpp.bypass.cooldown"))
                            PlayerCooldown.addCooldown(player, CooldownObject.PLAYER_SETTINGS, ConfigManager.getInt("PLAYER.SETTINGS-DELAY"));
                    } else
                        Common.sendMMMessage(player, LanguageManager.getString("PROFILE.NO-PERMISSION"));
                    break;
            }
        }
    }

    private static ItemStack getDuelRequestItem(boolean duelRequest) {
        if (duelRequest)
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.DUEL-REQUEST.ENABLED").get();

        return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.DUEL-REQUEST.DISABLED").get();
    }

    private static ItemStack getSidebarItem(boolean sidebar) {
        if (sidebar)
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.ENABLED").get();

        return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.DISABLED").get();
    }

    private static ItemStack getPartyInviteItem(boolean partyInvites) {
        if (partyInvites)
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.PARTY-INVITE.ENABLED").get();

        return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.PARTY-INVITE.DISABLED").get();
    }

    private static ItemStack getPrivateMessageItem(boolean privatMessage) {
        if (privatMessage)
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.PRIVATE-MESSAGE.ENABLED").get();

        return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.PRIVATE-MESSAGE.DISABLED").get();
    }

    private static ItemStack getHidePlayersItem(boolean hidePlayers) {
        if (!hidePlayers)
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.HIDE-PLAYERS.ENABLED").get();

        return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.HIDE-PLAYERS.DISABLED").get();
    }

    private static ItemStack getAllowSpectatorsItem(boolean allowSpectate) {
        if (allowSpectate)
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.ALLOW-SPECTATORS.ENABLED").get();

        return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.ALLOW-SPECTATORS.DISABLED").get();
    }

    private static ItemStack getFlyingItem(boolean flying) {
        if (flying) {
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.FLYING.ENABLED").get();
        } else {
            return GUIFile.getGuiItem("GUIS.PLAYER-SETTINGS.ICONS.SIDEBAR.FLYING.DISABLED").get();
        }
    }

}
