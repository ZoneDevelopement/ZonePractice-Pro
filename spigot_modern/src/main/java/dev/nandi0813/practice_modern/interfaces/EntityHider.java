package dev.nandi0813.practice_modern.interfaces;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class EntityHider extends dev.nandi0813.practice.module.interfaces.EntityHider implements Listener {

    public EntityHider(Plugin plugin, Policy policy) {
        super(plugin, policy);
    }

    @Override
    public void showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean hiddenBefore = !setVisibility(observer, entity.getEntityId(), true);

        // Resend packets
        if (hiddenBefore)
            observer.showEntity(plugin, entity);
    }

    @Override
    public void hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean visibleBefore = setVisibility(observer, entity.getEntityId(), false);

        if (visibleBefore)
            observer.hideEntity(plugin, entity);
    }

}
