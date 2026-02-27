package dev.nandi0813.practice.manager.ladder.abstraction.normal;

import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.ladder.LadderPreviewGui;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.enums.WeightClassType;
import dev.nandi0813.practice.util.BasicItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public abstract class NormalLadder extends Ladder {

    protected final LadderFile ladderFile;

    // Settings the admins can set.
    @Setter
    protected WeightClassType weightClass = WeightClassType.UNRANKED;
    protected boolean frozen; // If the ladder is frozen, players can't start a new game with it.
    @Setter
    protected boolean editable = true;

    protected List<BasicItem> destroyableBlocks = new ArrayList<>();

    // Extra items for custom kits
    protected Map<Boolean, ItemStack[]> customKitExtraItems = new HashMap<>();

    // Preview gui
    @Setter
    protected LadderPreviewGui previewGui;

    /**
     * Names of arenas this ladder was assigned to at the time it was disabled.
     * Populated by {@link dev.nandi0813.practice.manager.arena.ArenaManager#removeLadder}
     * and consumed (then cleared) by
     * {@link dev.nandi0813.practice.manager.ladder.util.LadderUtil#enableLadder} so the
     * assignments are automatically restored when the ladder is re-enabled.
     */
    protected final Set<String> previouslyAssignedArenas = new HashSet<>();

    protected NormalLadder(String name, LadderType type) {
        super(name, type);

        this.ladderFile = new LadderFile(this);
        this.build = type.isBuild();
        this.getData();

        this.previewGui = new LadderPreviewGui(this);
    }

    public void setData() {
        ladderFile.setData();
    }

    public void getData() {
        ladderFile.getData();
    }

    public void deleteData() {
        if (!ladderFile.getFile().delete()) {
            System.out.println("Could not delete ladder file for ladder: " + this.getName());
        }
    }

    public boolean isReadyToEnable() {
        return icon != null && kitData.isSet() && !matchTypes.isEmpty();
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;

        GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();
        LadderSetupManager.getInstance().getLadderSetupGUIs().get(this).get(GUIType.Ladder_Main).update();
        GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();

        if (this.isRanked())
            GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
    }

    public boolean isUnranked() {
        return weightClass.equals(WeightClassType.UNRANKED) || weightClass.equals(WeightClassType.UNRANKED_AND_RANKED);
    }

    public boolean isRanked() {
        return weightClass.equals(WeightClassType.RANKED) || weightClass.equals(WeightClassType.UNRANKED_AND_RANKED);
    }

    @Override
    public List<Arena> getArenas() {
        List<Arena> arenas = new ArrayList<>();
        for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
            if (arena.isEnabled()) {
                if (arena.getAssignedLadders().contains(this)) {
                    arenas.add(arena);
                }
            }
        }
        return arenas;
    }

}
