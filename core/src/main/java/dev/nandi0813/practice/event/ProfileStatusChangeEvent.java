package dev.nandi0813.practice.event;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class ProfileStatusChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Profile profile;
    private final ProfileStatus oldStatus;
    private final ProfileStatus newStatus;

    public ProfileStatusChangeEvent(Profile profile, ProfileStatus oldStatus, ProfileStatus newStatus) {
        this.profile = profile;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
