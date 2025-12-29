package dev.nandi0813.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.Match;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.Inventory;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.inventory.inventoryitem.InvItem;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import org.bukkit.entity.Player;

public class ShowSpectatorsInvItem extends InvItem {

    public ShowSpectatorsInvItem() {
        super(getItemStack("SPECTATOR.MATCH.NORMAL.SHOW-SPECTATORS.ITEM"), getInt("SPECTATOR.MATCH.NORMAL.SHOW-SPECTATORS.SLOT"));
    }

    @Override
    public void handleClickEvent(Player player) {
        if (!player.hasPermission("zpp.spectate.vanish")) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.NO-PERMISSIONS"));
            return;
        }

        if (!player.hasPermission("zpp.bypass.cooldown") && PlayerCooldown.isActive(player, CooldownObject.SPECTATOR_VANISH)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("SPECTATE.VANISH-COOLDOWN"), PlayerCooldown.getLeftInDouble(player, CooldownObject.SPECTATOR_VANISH)));
            return;
        } else
            PlayerCooldown.addCooldown(player, CooldownObject.SPECTATOR_VANISH, ConfigManager.getInt("SPECTATOR-SETTINGS.VANISH-COOLDOWN"));

        Profile profile = ProfileManager.getInstance().getProfile(player);

        profile.setHideSpectators(!profile.isHideSpectators());
        InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.SPECTATE_MATCH);
        PlayerHider.getInstance().toggleSpectatorVisibility(player);
    }
}
