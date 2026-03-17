package dev.nandi0813.practice.manager.fight.event;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventType;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.BracketsData;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.BracketsListener;
import dev.nandi0813.practice.manager.fight.event.events.duel.sumo.Sumo;
import dev.nandi0813.practice.manager.fight.event.events.duel.sumo.SumoData;
import dev.nandi0813.practice.manager.fight.event.events.duel.sumo.SumoListener;
import dev.nandi0813.practice.manager.fight.event.events.ffa.lms.LMS;
import dev.nandi0813.practice.manager.fight.event.events.ffa.lms.LMSData;
import dev.nandi0813.practice.manager.fight.event.events.ffa.lms.LMSListener;
import dev.nandi0813.practice.manager.fight.event.events.ffa.oitc.OITC;
import dev.nandi0813.practice.manager.fight.event.events.ffa.oitc.OITCData;
import dev.nandi0813.practice.manager.fight.event.events.ffa.oitc.OITCListener;
import dev.nandi0813.practice.manager.fight.event.events.ffa.splegg.Splegg;
import dev.nandi0813.practice.manager.fight.event.events.ffa.splegg.SpleggData;
import dev.nandi0813.practice.manager.fight.event.events.ffa.splegg.SpleggListener;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut.Juggernaut;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut.JuggernautData;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut.JuggernautListener;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag.TNTTag;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag.TNTTagData;
import dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag.TNTTagListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventListenerInterface;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.guis.EventHostGui;
import dev.nandi0813.practice.manager.gui.setup.event.EventSetupManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StartUpCallback;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class EventManager {

    private static EventManager instance;

    public static EventManager getInstance() {
        if (instance == null)
            instance = new EventManager();
        return instance;
    }

    private final List<Event> events;
    private final Map<EventType, EventData> eventData;
    private final Map<EventType, EventListenerInterface> eventListeners;

    private final EventListener listener;

    @Getter
    private AutoEventScheduler autoEventScheduler;

    public static final ItemStack PLAYER_TRACKER = ConfigManager.getGuiItem("EVENT.PLAYER-TRACKER").get();

    private EventManager() {
        this.events = new ArrayList<>();
        this.eventData = new HashMap<>();
        this.eventListeners = new HashMap<>();

        this.listener = new EventListener(this);
        Bukkit.getServer().getPluginManager().registerEvents(this.listener, ZonePractice.getInstance());
    }

    public void loadEventData(final StartUpCallback startUpCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            this.eventData.put(EventType.BRACKETS, new BracketsData());
            this.eventListeners.put(EventType.BRACKETS, new BracketsListener());

            this.eventData.put(EventType.SUMO, new SumoData());
            this.eventListeners.put(EventType.SUMO, new SumoListener());

            this.eventData.put(EventType.LMS, new LMSData());
            this.eventListeners.put(EventType.LMS, new LMSListener());

            this.eventData.put(EventType.OITC, new OITCData());
            this.eventListeners.put(EventType.OITC, new OITCListener());

            this.eventData.put(EventType.SPLEGG, new SpleggData());
            this.eventListeners.put(EventType.SPLEGG, new SpleggListener());

            this.eventData.put(EventType.JUGGERNAUT, new JuggernautData());
            this.eventListeners.put(EventType.JUGGERNAUT, new JuggernautListener());

            this.eventData.put(EventType.TNTTAG, new TNTTagData());
            this.eventListeners.put(EventType.TNTTAG, new TNTTagListener());

            for (EventData data : this.eventData.values()) {
                data.getData();
            }

            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                autoEventScheduler = new AutoEventScheduler();
                autoEventScheduler.start();
                startUpCallback.onLoadingDone();
            });
        });
    }

    public void loadGUIs() {
        GUIManager.getInstance().addGUI(new EventHostGui());
        EventSetupManager.getInstance().loadGUIs();

        // Initialize the wand-based setup manager
        dev.nandi0813.practice.manager.fight.event.setup.EventWandSetupManager.getInstance();
    }

    public void endEvents() {
        if (autoEventScheduler != null) {
            autoEventScheduler.cancel();
        }
        for (Event event : new java.util.ArrayList<>(events)) {
            event.forceEnd(null);
        }
    }

    public void saveEventData() {
        for (EventData data : eventData.values()) {
            data.setData();
        }
    }

    public boolean startEvent(Player starter, EventType eventType) {
        if (eventType == null) {
            return false;
        }

        if (!getEventData().get(eventType).isEnabled()) {
            ZonePractice.getInstance().getLogger().warning("Event " + eventType.getName() + " is not enabled.");
            return false;
        }

        if (starter != null) {
            if (!starter.hasPermission("zpp.event.host") ||
                    (!starter.hasPermission("zpp.event.host." + eventType.name().toLowerCase()) && !starter.hasPermission("zpp.event.host.all"))) {
                Common.sendMMMessage(starter, LanguageManager.getString("EVENT.CANT-HOST-EVENT").replace("%event%", eventType.getName()));
                return false;
            }

            Profile starterProfile = ProfileManager.getInstance().getProfile(starter);
            if (starterProfile == null || starterProfile.getEventStartLeft() <= 0) {
                Common.sendMMMessage(starter, LanguageManager.getString("EVENT.CANT-HOST-EVENT-TODAY"));
                return false;
            }
        }

        if (!this.events.isEmpty() && ConfigManager.getBoolean("EVENT.MULTIPLE")) {
            for (Event liveEvent : this.events) {
                if (liveEvent.getStatus().equals(dev.nandi0813.practice.manager.fight.event.enums.EventStatus.COLLECTING)) {
                    if (starter != null) {
                        Common.sendMMMessage(starter, LanguageManager.getString("COMMAND.EVENT.ARGUMENTS.HOST.CANT-HOST-NOW"));
                    }
                    return false;
                }
            }
        } else if (!this.events.isEmpty() && !ConfigManager.getBoolean("EVENT.MULTIPLE")) {
            if (starter != null) {
                Common.sendMMMessage(starter, LanguageManager.getString("EVENT.ONLY-ONE-EVENT"));
            }
            return false;
        }

        if (this.isEventLive(eventType)) {
            if (starter != null)
                Common.sendMMMessage(starter, LanguageManager.getString("EVENT.CANT-START-EVENT").replace("%event%", eventType.getName()));
            else
                Common.sendConsoleMMMessage(LanguageManager.getString("EVENT.CANT-START-EVENT").replace("%event%", eventType.getName()));

            return false;
        }

        Event event = switch (eventType) {
            case LMS -> new LMS(starter, (LMSData) eventData.get(EventType.LMS));
            case OITC -> new OITC(starter, (OITCData) eventData.get(EventType.OITC));
            case TNTTAG -> new TNTTag(starter, (TNTTagData) eventData.get(EventType.TNTTAG));
            case BRACKETS -> new Brackets(starter, (BracketsData) eventData.get(EventType.BRACKETS));
            case SUMO -> new Sumo(starter, (SumoData) eventData.get(EventType.SUMO));
            case SPLEGG -> new Splegg(starter, (SpleggData) eventData.get(EventType.SPLEGG));
            case JUGGERNAUT -> new Juggernaut(starter, (JuggernautData) eventData.get(EventType.JUGGERNAUT));
        };

        events.add(event);
        if (!event.startQueue()) {
            events.remove(event);
            return false;
        }

        if (starter != null) {
            Profile starterProfile = ProfileManager.getInstance().getProfile(starter);
            if (starterProfile != null) {
                starterProfile.setEventStartLeft(Math.max(0, starterProfile.getEventStartLeft() - 1));
            }
        }

        return true;
    }

    public boolean isEventLive(EventType eventType) {
        for (Event event : events) {
            if (event.getType().equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    public List<EventData> getEnabledEvents() {
        List<EventData> enabledEvents = new ArrayList<>();
        for (EventData eventData : eventData.values()) {
            if (eventData.isEnabled()) {
                enabledEvents.add(eventData);
            }
        }
        return enabledEvents;
    }

    public Event getEventByPlayer(Player player) {
        for (Event event : events) {
            if (event.getPlayers().contains(player)) {
                return event;
            }
        }
        return null;
    }

    public Event getEventBySpectator(Player player) {
        for (Event event : events) {
            if (event.getSpectators().contains(player)) {
                return event;
            }
        }
        return null;
    }

}
