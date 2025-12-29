package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerKitEditing {

    private final CustomLadder customLadder;
    @Setter
    private KitItem kitItem;

    public PlayerKitEditing(CustomLadder customLadder) {
        this.customLadder = customLadder;
    }

}
