package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.manager.profile.Profile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ActionBar extends dev.nandi0813.practice.module.interfaces.actionbar.ActionBar {

    public ActionBar(Profile profile) {
        super(profile);
    }

    @Override
    public void send() {
        Player player = profile.getPlayer().getPlayer();
        if (player != null) {
            player.sendActionBar(this.message);
        }
    }

    @Override
    public void clear() {
        Player player = profile.getPlayer().getPlayer();
        if (player != null) {
            player.sendActionBar(Component.empty());
        }
    }

}
