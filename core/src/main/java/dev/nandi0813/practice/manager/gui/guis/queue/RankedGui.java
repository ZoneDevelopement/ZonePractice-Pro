package dev.nandi0813.practice.manager.gui.guis.queue;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.queue.QueueManager;
import org.bukkit.entity.Player;

public class RankedGui extends QueueSelectorGui {

    public RankedGui() {
        super(GUIType.Queue_Ranked);
    }

    @Override
    protected long getUpdateCooldownMinutes() {
        return ConfigManager.getInt("QUEUE.RANKED.GUI-UPDATE-MINUTE");
    }

    @Override
    protected String getQueueConfigPath() {
        return "QUEUE.RANKED";
    }

    @Override
    protected String getGuiConfigPath() {
        return "GUIS.RANKED-GUI";
    }

    @Override
    protected WeightClass getWeightClass() {
        return WeightClass.RANKED;
    }

    @Override
    protected boolean isRanked() {
        return true;
    }

    @Override
    protected boolean isValidLadder(NormalLadder ladder) {
        return ladder.isRanked();
    }

    @Override
    protected void onLadderClick(Player player, NormalLadder ladder) {
        QueueManager.getInstance().createRankedQueue(player, ladder);
    }
}