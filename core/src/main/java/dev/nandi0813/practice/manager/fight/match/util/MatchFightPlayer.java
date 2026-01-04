package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.fight.util.FightPlayer;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MatchFightPlayer extends FightPlayer {

    private static final GUIItem CUSTOM_KIT_ITEM = ConfigManager.getGuiItem("MATCH-SETTINGS.KIT-ITEMS.CUSTOM-KIT");
    private static final GUIItem DEFAULT_KIT_ITEM = ConfigManager.getGuiItem("MATCH-SETTINGS.KIT-ITEMS.DEFAULT-KIT");

    private final Match match;
    private final Ladder ladder;

    private Map<Integer, CustomKit> kits;

    @Getter
    private boolean hasChosenKit;
    private int chosenKit;

    public MatchFightPlayer(Player player, Match match) {
        super(player, match);

        this.match = match;
        this.ladder = match.getLadder();

        this.hasChosenKit = true;
        if (this.getProfile().getAllowedCustomKits() >= 1 && ladder instanceof NormalLadder) {
            this.kits = new HashMap<>();
            this.loadKits();
        }
    }

    public void loadKits() {
        if (this.kits == null) {
            return;
        }

        Map<Integer, CustomKit> customKits = null;
        if (ladder instanceof NormalLadder normalLadder) {
            if (match instanceof Duel duel && duel.isRanked()) {
                customKits = profile.getRankedCustomKits().get(normalLadder);
            } else {
                customKits = profile.getUnrankedCustomKits().get(normalLadder);
            }
        }

        if (customKits != null && !customKits.isEmpty()) {
            for (Map.Entry<Integer, CustomKit> customKit : customKits.entrySet()) {
                this.kits.put(customKit.getKey() - 1, new CustomKit(
                        CUSTOM_KIT_ITEM.cloneItem().replace("%kit%", String.valueOf(customKit.getKey())).get(),
                        customKit.getValue().getInventory(),
                        customKit.getValue().getExtra()));
            }

            this.kits.put(8, new CustomKit(DEFAULT_KIT_ITEM.cloneItem().get(), ladder.getKitData().getStorage(), ladder.getKitData().getExtra()));

            this.hasChosenKit = false;
        }
    }

    public void setKitChooserOrKit(TeamEnum team) {
        player.getInventory().clear();

        if (this.kits != null && !this.kits.isEmpty()) {
            if (!this.hasChosenKit) {
                for (Map.Entry<Integer, CustomKit> kit : this.kits.entrySet()) {
                    player.getInventory().setItem(kit.getKey(), kit.getValue().getBook());
                }
            } else if (this.kits.containsKey(this.chosenKit)) {
                CustomKit kit = this.kits.get(this.chosenKit);
                KitUtil.loadKit(player, team, ladder.getKitData().getArmor(), kit.getInventory(), kit.getExtra());
            } else {
                KitUtil.loadDefaultLadderKit(player, team, ladder);
            }
        } else {
            KitUtil.loadDefaultLadderKit(player, team, ladder);
        }
    }

    public void setChosenKit(int slot, TeamEnum team) {
        if (this.kits != null && this.kits.containsKey(slot)) {
            this.hasChosenKit = true;
            this.chosenKit = slot;

            setKitChooserOrKit(team);
        }
    }

}
