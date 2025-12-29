package dev.nandi0813.practice.module.interfaces;

import org.bukkit.entity.Player;

public interface PlayerHiderInterface {

    void hidePlayer(Player observer, Player target, boolean fullHide);

    void showPlayer(Player observer, Player target);

}
