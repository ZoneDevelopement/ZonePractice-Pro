package dev.nandi0813.practice.Manager.Party;

import dev.nandi0813.practice.Manager.Backend.ConfigManager;
import dev.nandi0813.practice.Manager.Backend.LanguageManager;
import dev.nandi0813.practice.Manager.Fight.Match.Match;
import dev.nandi0813.practice.Manager.GUI.GUIManager;
import dev.nandi0813.practice.Manager.GUI.GUIType;
import dev.nandi0813.practice.Manager.GUI.GUIs.Party.PartySettingsGui;
import dev.nandi0813.practice.Manager.Inventory.InventoryManager;
import dev.nandi0813.practice.Manager.Profile.Enum.ProfileStatus;
import dev.nandi0813.practice.Manager.Profile.Profile;
import dev.nandi0813.practice.Manager.Profile.ProfileManager;
import dev.nandi0813.practice.Util.Common;
import dev.nandi0813.practice.Util.PlayerUtil.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Party implements dev.nandi0813.api.Interface.Party {

    private static final int DEFAULT_MAX_PARTY_MEMBERS = ConfigManager.getInt("PARTY.SETTINGS.MAX-PARTY-MEMBERS.DEFAULT");
    private static final boolean DEFAULT_PUBLIC_PARTY = ConfigManager.getBoolean("PARTY.SETTINGS.DEFAULT.PUBLIC-PARTY");
    private static final boolean DEFAULT_ALL_INVITE = ConfigManager.getBoolean("PARTY.SETTINGS.DEFAULT.ALL-INVITE");
    private static final boolean DEFAULT_PARTY_CHAT = ConfigManager.getBoolean("PARTY.SETTINGS.DEFAULT.PARTY-CHAT");
    private static final boolean DEFAULT_DUEL_REQUEST = ConfigManager.getBoolean("PARTY.SETTINGS.DEFAULT.DUEL-REQUESTS");

    @Setter
    private Player leader;
    private final List<Player> members = new ArrayList<>();
    private final Map<Player, Long> invites = new HashMap<>();

    @Setter
    private BroadcastTask broadcastTask = new BroadcastTask(this);
    private final PartySettingsGui partySettingsGui;

    @Setter
    private int maxPlayerLimit;
    @Setter
    private boolean publicParty;
    @Setter
    private boolean broadcastParty;
    @Setter
    private boolean allInvite;
    @Setter
    private boolean partyChat;
    @Setter
    private boolean duelRequests;

    @Setter
    private Match match;

    public Party(Player owner) {
        this.leader = owner;
        this.members.add(owner);

        this.maxPlayerLimit = DEFAULT_MAX_PARTY_MEMBERS;
        this.publicParty = DEFAULT_PUBLIC_PARTY;
        this.broadcastParty = DEFAULT_PUBLIC_PARTY;
        this.allInvite = DEFAULT_ALL_INVITE;
        this.partyChat = DEFAULT_PARTY_CHAT;
        this.duelRequests = DEFAULT_DUEL_REQUEST;

        this.partySettingsGui = (PartySettingsGui) GUIManager.getInstance().addGUI(new PartySettingsGui(this));
    }

    public void setNewOwner(Player newOwner) {
        if (broadcastTask.isRunning())
            broadcastTask.cancel();

        leader = newOwner;
        sendMessage(LanguageManager.getString("PARTY.NEW-LEADER").replaceAll("%player%", newOwner.getName()));
    }

    public void addMember(Player member) {
        Profile memberProfile = ProfileManager.getInstance().getProfile(member);

        members.add(member);
        memberProfile.setParty(true);
        InventoryManager.getInstance().setLobbyInventory(member, false);

        sendMessage(LanguageManager.getString("PARTY.PLAYER-JOINED").replaceAll("%player%", member.getName()));
        GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).update();

        // If the party is full, stop broadcasting it.
        if (members.size() == maxPlayerLimit && isBroadcastParty())
            broadcastTask.cancel();
    }

    public void removeMember(Player member, boolean kick) {
        Profile memberProfile = ProfileManager.getInstance().getProfile(member);

        if (kick)
            sendMessage(LanguageManager.getString("PARTY.PLAYER-KICKED").replaceAll("%player%", member.getName()));
        else
            sendMessage(LanguageManager.getString("PARTY.PLAYER-LEFT").replaceAll("%player%", member.getName()));

        if (member.equals(leader))
            disband();

        members.remove(member);
        memberProfile.setParty(false);

        if (ProfileManager.getInstance().getProfile(member).getStatus().equals(ProfileStatus.LOBBY))
            InventoryManager.getInstance().setLobbyInventory(member, false);

        GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).update();
    }

    public void disband() {
        broadcastTask.cancel();
        sendMessage(LanguageManager.getString("PARTY.PARTY-DISBANDED"));

        for (Player member : members)
            ProfileManager.getInstance().getProfile(member).setParty(false);

        List<Player> members_copy = new ArrayList<>(members);
        members.clear();

        if (match != null) {
            if (ConfigManager.getBoolean("PARTY.END-MATCH-ON-DISBAND")) {
                match.sendMessage(LanguageManager.getString("PARTY.MATCH-END"), true);
                match.endMatch();
            }
        } else {
            for (Player member : members_copy)
                InventoryManager.getInstance().setLobbyInventory(member, false);
        }

        GUIManager.getInstance().searchGUI(GUIType.Party_OtherParties).update();
        PartyManager.getInstance().getParties().remove(this);
    }

    public void sendMessage(String message) {
        for (Player player : members)
            Common.sendMMMessage(player, message);
    }

    public List<String> getMemberNames() {
        return PlayerUtil.getPlayerNames(members);
    }

}
