package dev.nandi0813.practice.manager.gui.guis.queue;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.enums.WeightClass;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.queue.QueueManager;
import org.bukkit.entity.Player;

public class UnrankedGui extends QueueSelectorGui {

    public UnrankedGui() {
        super(GUIType.Queue_Unranked);
    }

    @Override
    protected long getUpdateCooldownMinutes() {
        return ConfigManager.getInt("QUEUE.UNRANKED.GUI-UPDATE-MINUTE");
    }

    @Override
    protected String getQueueConfigPath() {
        return "QUEUE.UNRANKED";
    }

    @Override
    protected String getGuiConfigPath() {
        return "GUIS.UNRANKED-GUI";
    }

    @Override
    protected WeightClass getWeightClass() {
        return WeightClass.UNRANKED;
    }

    @Override
    protected boolean isRanked() {
        return false;
    }

    @Override
    protected boolean isValidLadder(NormalLadder ladder) {
        return ladder.isUnranked();
    }

    @Override
    protected void onLadderClick(Player player, NormalLadder ladder) {
        QueueManager.getInstance().createUnrankedQueue(player, ladder);
    }
}