package dev.nandi0813.practice.manager.gui.guis.party;

import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.selectors.LadderSelectorGui;
import dev.nandi0813.practice.manager.party.Party;
import dev.nandi0813.practice.manager.party.PartyManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PartyEventsGui extends GUI {

    private static final ItemStack FILLER_ITEM = GUIFile.getGuiItem("GUIS.PARTY.PARTY-GAMES.ICONS.FILLER-ITEM").get();

    public PartyEventsGui() {
        super(GUIType.Party_Events);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.PARTY.PARTY-GAMES.TITLE"), 3));

        build();
    }

    @Override
    public void build() {
        update();
    }

    @Override
    public void update() {
        Inventory inventory = gui.get(1);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, FILLER_ITEM);
        }

        inventory.setItem(11, GUIFile.getGuiItem("GUIS.PARTY.PARTY-GAMES.ICONS.PARTY_SPLIT").get());
        inventory.setItem(15, GUIFile.getGuiItem("GUIS.PARTY.PARTY-GAMES.ICONS.PARTY_FFA").get());

        updatePlayers();
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Party party = PartyManager.getInstance().getParty(player);

        if (party == null) return;

        int slot = e.getRawSlot();
        e.setCancelled(true);

        switch (slot) {
            case 11:
                new LadderSelectorGui(profile, MatchType.PARTY_SPLIT).open(player);
                break;
            case 15:
                new LadderSelectorGui(profile, MatchType.PARTY_FFA).open(player);
                break;
        }
    }

}
