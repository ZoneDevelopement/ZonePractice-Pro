package dev.nandi0813.practice.manager.gui.guis.party;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PartySettingsGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.FILLER-ITEM").get();
    private final Party party;

    public PartySettingsGui(Party party) {
        super(GUIType.Party_Settings);
        this.party = party;

        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PARTY.PARTY-SETTINGS.TITLE"), 3));

        build();
    }

    @Override
    public void build() {
        for (int i = 0; i < gui.get(1).getSize(); i++) {
            gui.get(1).setItem(i, FILLER_ITEM);
        }

        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        if (party.isDuelRequests()) {
            inventory.setItem(10, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.DUEL-REQUESTS.ENABLED").get());
        } else
            inventory.setItem(10, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.DUEL-REQUESTS.DISABLED").get());

        inventory.setItem(11, getPlayerLimitItem(party.getMaxPlayerLimit()));

        if (party.isPartyChat())
            inventory.setItem(12, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.PARTY-CHAT.ENABLED").get());
        else
            inventory.setItem(12, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.PARTY-CHAT.DISABLED").get());

        if (party.isAllInvite())
            inventory.setItem(14, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.ALL-INVITE.ENABLED").get());
        else
            inventory.setItem(14, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.ALL-INVITE.DISABLED").get());

        if (party.isPublicParty())
            inventory.setItem(15, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.ACCESS-MODIFIERS.PUBLIC").get());
        else
            inventory.setItem(15, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.ACCESS-MODIFIERS.PRIVATE").get());

        if (party.isBroadcastParty())
            inventory.setItem(16, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.BROADCAST.ENABLED").get());
        else
            inventory.setItem(16, GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.BROADCAST.DISABLED").get());

        this.updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Party party = PartyManager.getInstance().getParty(player);

        ClickType clickType = e.getClick();
        ItemStack currentItem = e.getCurrentItem();
        int slot = e.getRawSlot();
        e.setCancelled(true);

        if (party == null) return;
        if (currentItem == null) return;

        switch (slot) {
            case 10:
                if (player.hasPermission("zpp.party.duelrequest")) {
                    party.setDuelRequests(!party.isDuelRequests());
                    update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
            case 11:
                if (player.hasPermission("zpp.party.changelimit")) {
                    if (clickType.isLeftClick() && party.getMaxPlayerLimit() > 2) {
                        if (party.getMembers().size() < party.getMaxPlayerLimit()) {
                            party.setMaxPlayerLimit(party.getMaxPlayerLimit() - 1);
                            update();
                        } else
                            Common.sendMMMessage(player, LanguageManager.getString("PARTY.CANT-DECREASE-LIMIT"));
                    } else if (clickType.isRightClick() && party.getMaxPlayerLimit() < PartyManager.MAX_PARTY_MEMBERS) {
                        party.setMaxPlayerLimit(party.getMaxPlayerLimit() + 1);
                        update();
                    }
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
                break;
            case 12:
                if (player.hasPermission("zpp.party.partychat")) {
                    party.setPartyChat(!party.isPartyChat());
                    update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
            case 14:
                if (player.hasPermission("zpp.party.allinvite")) {
                    party.setAllInvite(!party.isAllInvite());
                    update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
            case 15:
                if (player.hasPermission("zpp.party.public")) {
                    if (PlayerCooldown.isActive(player, CooldownObject.PUBLIC_PARTY_CHANGE)) {
                        Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("WAIT-FOR-COOLDOWN"), PlayerCooldown.getLeftInDouble(player, CooldownObject.LEADERBOARD_GUI_REFRESH)));
                        return;
                    }

                    PlayerCooldown.addCooldown(player, CooldownObject.PUBLIC_PARTY_CHANGE, 5);

                    if (party.isPublicParty() && party.isBroadcastParty())
                        party.getBroadcastTask().cancel();

                    party.setPublicParty(!party.isPublicParty());
                    update();
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
                break;
            case 16:
                if (player.hasPermission("zpp.party.broadcast")) {
                    if (PlayerCooldown.isActive(player, CooldownObject.BROADCAST_PARTY_CHANGE)) {
                        Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("WAIT-FOR-COOLDOWN"), PlayerCooldown.getLeftInDouble(player, CooldownObject.LEADERBOARD_GUI_REFRESH)));
                        return;
                    }

                    PlayerCooldown.addCooldown(player, CooldownObject.BROADCAST_PARTY_CHANGE, 5);

                    if (party.isBroadcastParty()) {
                        party.getBroadcastTask().cancel();
                        update();
                    } else {
                        if (party.isPublicParty()) {
                            party.getBroadcastTask().begin();
                            update();
                        } else
                            Common.sendMMMessage(player, LanguageManager.getString("PARTY.PARTY-NOT-PUBLIC"));
                    }
                } else
                    Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
                break;
        }
    }

    public ItemStack getPlayerLimitItem(int limit) {
        return GUIFile.getGuiItem("GUIS.PARTY.PARTY-SETTINGS.ICONS.PLAYER-LIMIT")
                .replace("%limit%", String.valueOf(limit))
                .get();
    }

}
