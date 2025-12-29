package dev.nandi0813.practice.manager.party;

import dev.nandi0813.api.Event.PartyCreateEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.party.OtherPartiesGui;
import dev.nandi0813.practice.manager.gui.guis.party.PartyEventsGui;
import dev.nandi0813.practice.manager.inventory.InventoryManager;
import dev.nandi0813.practice.manager.party.matchrequest.RequestManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PartyManager implements Listener {

    private static PartyManager instance;

    public static PartyManager getInstance() {
        if (instance == null)
            instance = new PartyManager();
        return instance;
    }

    private final RequestManager requestManager = new RequestManager();
    private final List<Party> parties = new ArrayList<>();

    public static final long INVITE_COOLDOWN = ConfigManager.getInt("PARTY.PARTY-INVITE-COOLDOWN") * 1000L;
    public static final int MAX_PARTY_MEMBERS = ConfigManager.getInt("PARTY.SETTINGS.MAX-PARTY-MEMBERS.PERMISSION");

    private PartyManager() {
        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());

        GUIManager.getInstance().addGUI(new OtherPartiesGui());
        GUIManager.getInstance().addGUI(new PartyEventsGui());
    }

    public Party getParty(Player player) {
        for (Party party : parties)
            if (party.getMembers().contains(player))
                return party;
        return null;
    }

    public Party getParty(Match match) {
        for (Party party : parties)
            if (party.getMatch() != null && party.getMatch().equals(match))
                return party;
        return null;
    }

    public void createParty(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!player.hasPermission("zpp.party.create")) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.NO-PERMISSION"));
            return;
        }

        if (PartyManager.getInstance().getParty(player) != null) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.ALREADY-PARTY"));
            return;
        }

        if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
            Common.sendMMMessage(player, LanguageManager.getString("PARTY.CANT-CREATE-PARTY"));
            return;
        }

        Party party = new Party(player);
        PartyCreateEvent partyCreateEvent = new PartyCreateEvent(party);
        Bukkit.getPluginManager().callEvent(partyCreateEvent);

        if (!partyCreateEvent.isCancelled()) {
            parties.add(party);
            profile.setParty(true);

            InventoryManager.getInstance().setLobbyInventory(player, false);
            GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).update();

            Common.sendMMMessage(player, LanguageManager.getString("PARTY.PARTY-CREATED"));
        }
    }

    @EventHandler ( priority = EventPriority.HIGHEST, ignoreCancelled = true )
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Party party = this.getParty(player);

        if (party != null)
            party.removeMember(player, false);
    }

}
