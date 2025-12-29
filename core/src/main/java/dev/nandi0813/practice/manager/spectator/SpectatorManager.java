package dev.nandi0813.practice.manager.spectator;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.guis.SpectatorMenuGui;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class SpectatorManager {

    private static SpectatorManager instance;

    public static SpectatorManager getInstance() {
        if (instance == null)
            instance = new SpectatorManager();
        return instance;
    }

    private static final Random random = new Random();
    private final Map<Player, Spectatable> spectators = new HashMap<>();
    private final SpectatorMenuGui spectatorMenuGui;

    private SpectatorManager() {
        Bukkit.getPluginManager().registerEvents(new SpectatorListener(), ZonePractice.getInstance());

        this.spectatorMenuGui = (SpectatorMenuGui) GUIManager.getInstance().addGUI(new SpectatorMenuGui());
    }

    public void spectateMenuUse(Player player) {
        if (!player.hasPermission("zpp.spectate.menu")) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.NO-PERMISSIONS"));
            return;
        }

        spectatorMenuGui.open(player);
    }

    public static void spectateRandomMatchItemUse(Player player) {
        if (!player.hasPermission("zpp.spectate.random")) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.NO-PERMISSIONS"));
            return;
        }

        if (!player.hasPermission("zpp.bypass.cooldown") && PlayerCooldown.isActive(player, CooldownObject.RANDOM_MATCH)) {
            Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("SPECTATE.RANDOM-MATCH-COOLDOWN"), PlayerCooldown.getLeftInDouble(player, CooldownObject.RANDOM_MATCH)));
            return;
        }

        PlayerCooldown.addCooldown(player, CooldownObject.RANDOM_MATCH, ConfigManager.getInt("SPECTATOR-SETTINGS.RANDOM-MATCH-COOLDOWN"));
        spectateRandomMatch(player);
    }

    public static void spectateRandomMatch(Player player) {
        List<Spectatable> spectatables = new ArrayList<>(MatchManager.getInstance().getLiveMatches());
        spectatables.addAll(FFAManager.getInstance().getOpenFFAs());

        if (spectatables.isEmpty()) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.NO-MATCH"));
            return;
        }

        Spectatable match = null;
        if (spectatables.size() > 1) {
            boolean found = false;
            do {
                Spectatable randomMatch = spectatables.get(random.nextInt(spectatables.size()));
                if (!randomMatch.getSpectators().contains(player)) {
                    found = true;
                    match = randomMatch;
                }
            } while (!found);
        } else {
            match = spectatables.get(0);
        }

        if (match.getSpectators().contains(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("SPECTATE.MATCH.ONLY-ONE-TO-SPECTATE"));
            return;
        }

        match.addSpectator(player, null, true, false);
    }

}
