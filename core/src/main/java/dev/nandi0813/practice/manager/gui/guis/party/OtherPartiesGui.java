package dev.nandi0813.practice.manager.gui.guis.party;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.selectors.LadderSelectorGui;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.module.interfaces.ItemCreateUtil;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class OtherPartiesGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.PARTY.OTHER-PARTIES.ICONS.FILLER-ITEM").get();
    private final Map<Integer, Party> partySlots = new HashMap<>();

    public OtherPartiesGui() {
        super(GUIType.Party_OtherParties);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PARTY.OTHER-PARTIES.TITLE"), 6));

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
            inventory.clear();
            partySlots.clear();

            if (PartyManager.getInstance() == null) return;

            for (Party party : PartyManager.getInstance().getParties()) {
                int slot = inventory.firstEmpty();

                if (slot < inventory.getSize()) {
                    inventory.setItem(slot, getPartyItem(party));
                    partySlots.put(slot, party);
                }
            }

            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, FILLER_ITEM);
                }
            }

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);

        if (party == null) return;

        ItemStack currentItem = e.getCurrentItem();
        int slot = e.getRawSlot();
        e.setCancelled(true);

        if (currentItem == null) return;
        if (!partySlots.containsKey(slot)) return;

        Party targetParty = partySlots.get(slot);

        if (party == targetParty) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.CANT-DUEL-OWN-PARTY"));
            return;
        }

        if (!targetParty.isDuelRequests()) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.CANT-DUEL-PARTY"));
            return;
        }

        if (!PartyManager.getInstance().getRequestManager().isRequested(party, targetParty) || party.getLeader().hasPermission("zpp.party.infiniteinvite")) {
            PartyManager.getInstance().getRequestManager().getPendingRequestTarget().put(party, targetParty);
            new LadderSelectorGui(profile, MatchType.PARTY_VS_PARTY).open(player);
        }
    }

    public ItemStack getPartyItem(Party party) {
        ItemStack itemStack = ClassImport.getClasses().getItemMaterialUtil().getPlayerHead(party.getLeader());
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = new ArrayList<>();
        List<String> memberStrings = new ArrayList<>();
        int count = 0;
        for (Player player : party.getMembers()) {
            if (count < 6) {
                if (!party.getLeader().equals(player)) {
                    memberStrings.add(GUIFile.getString("GUIS.PARTY.OTHER-PARTIES.ICONS.PARTY-ITEM.LORE.MEMBER-FORMAT")
                            .replace("%player%", player.getName())
                    );
                    count++;
                }
            } else break;
        }

        for (String line : GUIFile.getStringList("GUIS.PARTY.OTHER-PARTIES.ICONS.PARTY-ITEM.LORE.LORE")) {
            if (line.contains("%members%"))
                lore.addAll(memberStrings);
            else
                lore.add(line);
        }

        itemMeta.setDisplayName(GUIFile.getString("GUIS.PARTY.OTHER-PARTIES.ICONS.PARTY-ITEM.NAME")
                .replace("%leader%", party.getLeader().getName())
                .replace("%partySize%", String.valueOf(party.getMembers().size()))
        );
        itemMeta.setLore(lore);

        ItemCreateUtil.hideItemFlags(itemMeta);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
