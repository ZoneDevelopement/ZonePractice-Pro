package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.util.CustomKit;
import dev.nandi0813.practice.manager.fight.match.util.KitUtil;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles custom kit selection for both Match and FFA players.
 * Players can choose from their saved custom kits, or use the default ladder kit.
 * Until a kit is selected, the player is in a "spectator-like" state.
 */
@Getter
public class KitSelectionHandler {

    private static final GUIItem CUSTOM_KIT_ITEM = ConfigManager.getGuiItem("MATCH-SETTINGS.KIT-ITEMS.CUSTOM-KIT");
    private static final GUIItem DEFAULT_KIT_ITEM = ConfigManager.getGuiItem("MATCH-SETTINGS.KIT-ITEMS.DEFAULT-KIT");

    private final Player player;
    private final Profile profile;
    private final Ladder ladder;
    
    private Map<Integer, CustomKit> kits;
    private boolean hasChosenKit;
    private int chosenKit;

    public KitSelectionHandler(Player player, Profile profile, Ladder ladder) {
        this.player = player;
        this.profile = profile;
        this.ladder = ladder;
        this.hasChosenKit = true;
        this.loadKits();
    }

    /**
     * Loads available custom kits for this player from their profile.
     */
    public void loadKits() {
        if (profile.getAllowedCustomKits() < 1 || !(ladder instanceof NormalLadder normalLadder)) {
            return;
        }

        Map<Integer, CustomKit> customKits = profile.getUnrankedCustomKits().get(normalLadder);
        
        if (customKits != null && !customKits.isEmpty()) {
            this.kits = new HashMap<>();
            
            for (Map.Entry<Integer, CustomKit> customKit : customKits.entrySet()) {
                this.kits.put(customKit.getKey() - 1, new CustomKit(
                        createKitBook(customKit.getKey()),
                        customKit.getValue().getInventory(),
                        customKit.getValue().getExtra()));
            }

            // Add default kit option at slot 8
            this.kits.put(8, new CustomKit(
                    createDefaultKitBook(),
                    ladder.getKitData().getStorage(),
                    ladder.getKitData().getExtra()));

            this.hasChosenKit = false;
        }
    }

    /**
     * Loads custom kits considering ranked status.
     */
    public void loadKitsForRanked(boolean isRanked) {
        if (profile.getAllowedCustomKits() < 1 || !(ladder instanceof NormalLadder normalLadder)) {
            return;
        }

        Map<Integer, CustomKit> customKits = isRanked 
            ? profile.getRankedCustomKits().get(normalLadder)
            : profile.getUnrankedCustomKits().get(normalLadder);
        
        if (customKits != null && !customKits.isEmpty()) {
            this.kits = new HashMap<>();
            
            for (Map.Entry<Integer, CustomKit> customKit : customKits.entrySet()) {
                this.kits.put(customKit.getKey() - 1, new CustomKit(
                        createKitBook(customKit.getKey()),
                        customKit.getValue().getInventory(),
                        customKit.getValue().getExtra()));
            }

            // Add default kit option at slot 8
            this.kits.put(8, new CustomKit(
                    createDefaultKitBook(),
                    ladder.getKitData().getStorage(),
                    ladder.getKitData().getExtra()));

            this.hasChosenKit = false;
        }
    }

    /**
     * Displays the kit chooser GUI or applies the chosen kit.
     * If hasChosenKit is false, shows enchanted books in the inventory.
     * If hasChosenKit is true, applies the chosen kit.
     */
    public void showKitChooserOrApplyKit(TeamEnum team) {
        player.getInventory().clear();

        if (this.kits != null && !this.kits.isEmpty()) {
            if (!this.hasChosenKit) {
                // Show kit selection GUI (enchanted books)
                for (Map.Entry<Integer, CustomKit> kit : this.kits.entrySet()) {
                    player.getInventory().setItem(kit.getKey(), kit.getValue().getBook());
                }
            } else if (this.kits.containsKey(this.chosenKit)) {
                // Apply the chosen custom kit
                CustomKit kit = this.kits.get(this.chosenKit);
                KitUtil.loadKit(player, team, ladder.getKitData().getArmor(), kit.getInventory(), kit.getExtra());
            } else {
                // Fallback to default ladder kit
                KitUtil.loadDefaultLadderKit(player, team, ladder);
            }
        } else {
            // No custom kits available, load default
            KitUtil.loadDefaultLadderKit(player, team, ladder);
        }
    }

    /**
     * Called when player clicks a kit slot to select it.
     */
    public void selectKit(int slot, TeamEnum team) {
        if (this.kits != null && this.kits.containsKey(slot)) {
            this.hasChosenKit = true;
            this.chosenKit = slot;
            showKitChooserOrApplyKit(team);
        }
    }

    /**
     * Returns whether the player is currently waiting to select a kit.
     */
    public boolean isWaitingForKitSelection() {
        return !hasChosenKit;
    }

    /**
     * Returns whether this handler has custom kits available.
     */
    public boolean hasCustomKits() {
        return kits != null && !kits.isEmpty();
    }

    private ItemStack createKitBook(int kitNumber) {
        GUIItem kitItem = CUSTOM_KIT_ITEM.cloneItem().replace("%kit%", String.valueOf(kitNumber));
        return kitItem.get();
    }

    private ItemStack createDefaultKitBook() {
        return DEFAULT_KIT_ITEM.cloneItem().get();
    }
}

