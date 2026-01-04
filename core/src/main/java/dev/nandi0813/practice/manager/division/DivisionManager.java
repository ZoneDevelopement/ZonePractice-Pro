package dev.nandi0813.practice.manager.division;

import dev.nandi0813.api.Event.Match.MatchEndEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DivisionManager extends ConfigFile implements Listener {

    private final boolean COUNT_BY_WINS;
    private final boolean COUNT_BY_ELO;

    private static DivisionManager instance;

    public static DivisionManager getInstance() {
        if (instance == null)
            instance = new DivisionManager();
        return instance;
    }

    private final boolean divisionsEnabled = ConfigManager.getBoolean("DIVISIONS.ENABLED");
    private final boolean newDivisionMessageEnabled = ConfigManager.getBoolean("DIVISIONS.NEW-DIVISION-MESSAGE");
    private Division minimumForRanked;

    private final List<Division> divisions = new ArrayList<>();

    private DivisionManager() {
        super("", "divisions");
        this.reloadFile();

        boolean COUNT_BY_WINS = this.getBoolean("REQUIRE.WINS");
        boolean COUNT_BY_ELO = this.getBoolean("REQUIRE.ELO");
        if (!COUNT_BY_WINS && !COUNT_BY_ELO) {
            COUNT_BY_WINS = true;
        }
        this.COUNT_BY_WINS = COUNT_BY_WINS;
        this.COUNT_BY_ELO = COUNT_BY_ELO;

        Bukkit.getPluginManager().registerEvents(this, ZonePractice.getInstance());
    }

    public void reloadRanks() {
        this.reloadFile();
        divisions.clear();
        this.getData();

        this.setDivisions();
    }

    public void setDivisions() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getStats().setDivision(this.getDivision(profile));
            }
        });
    }

    public Division getDivision(final Profile profile) {
        int experience = profile.getStats().getExperience();
        int wins = profile.getStats().getGlobalWins();
        int elo = profile.getStats().getGlobalElo();

        for (int i = 0; i < this.divisions.size(); i++) {
            Division division = this.divisions.get(i);
            Division nextDivision;
            try {
                nextDivision = this.divisions.get(i + 1);
            } catch (IndexOutOfBoundsException e) {
                nextDivision = null;
            }

            boolean meetsExperience = division.getExperience() <= experience;
            boolean meetsWins = !COUNT_BY_WINS || division.getWin() <= wins;
            boolean meetsElo = !COUNT_BY_ELO || division.getElo() <= elo;

            if (meetsExperience && meetsWins && meetsElo) {
                if (nextDivision == null) {
                    return division;
                } else {
                    boolean nextExperience = nextDivision.getExperience() > experience;
                    boolean nextWins = COUNT_BY_WINS && nextDivision.getWin() > wins;
                    boolean nextElo = COUNT_BY_ELO && nextDivision.getElo() > elo;

                    if (nextExperience || nextWins || nextElo) {
                        return division;
                    }
                }
            }
        }

        return null;
    }

    public Division getNextDivision(final Profile profile) {
        return getDivision(profile.getStats().getDivision());
    }

    public Division getNextDivision(final Division division) {
        return getDivision(division);
    }

    private Division getDivision(Division division) {
        if (division == null) return null;

        int index = this.divisions.indexOf(division);
        if (index == -1) return null;

        if (index + 1 >= this.divisions.size()) return null;
        return this.divisions.get(index + 1);
    }

    public Division getPreviousDivision(final Division division) {
        if (division == null) return null;

        int index = this.divisions.indexOf(division);
        if (index == -1) return null;

        if (index - 1 < 0) return null;
        return this.divisions.get(index - 1);
    }

    @Override
    public void setData() {
    }

    @Override
    public void getData() {
        int count = 0;
        for (String divisionConfigName : config.getConfigurationSection("DIVISIONS").getKeys(false)) {
            if (count == 28) break;

            Division division = new Division(divisionConfigName, config.getConfigurationSection("DIVISIONS." + divisionConfigName));
            if (!division.isValid()) continue;

            divisions.add(division);

            count++;
        }

        divisions.sort(new DivisionComparator());
        this.minimumForRanked = getDivisionByConfigName(ConfigManager.getString("DIVISIONS.MIN-DIVISION-FOR-RANKED"));
    }

    private Division getDivisionByConfigName(final String divisionConfigName) {
        for (Division division : this.divisions)
            if (division.getConfigName().equalsIgnoreCase(divisionConfigName))
                return division;
        return null;
    }

    @EventHandler
    public void onMatchEnd(MatchEndEvent e) {
        Match match = (Match) e.getMatch();
        if (!(match.getLadder() instanceof NormalLadder)) {
            return;
        }

        int expReceived = match.getDuration();

        for (Player player : match.getPlayers()) {
            Profile profile = ProfileManager.getInstance().getProfile(player);
            profile.getStats().setExperience(profile.getStats().getExperience() + expReceived);

            Common.sendMMMessage(player, LanguageManager.getString("MATCH.EXP-RECEIVED").replace("%exp%", String.valueOf(expReceived)));

            Division oldDivision = profile.getStats().getDivision();
            Division newDivision = this.getDivision(profile);

            if (oldDivision != newDivision) {
                profile.getStats().setDivision(newDivision);

                if (this.newDivisionMessageEnabled) {
                    Common.sendMMMessage(player, LanguageManager.getString("MATCH.REACHED-NEW-DIVISION")
                            .replace("%newDivision_fullName%", newDivision.getFullName())
                            .replace("%newDivision_shortName%", newDivision.getShortName())
                            .replace("%oldDivision_fullName%", oldDivision.getFullName())
                            .replace("%oldDivision_shortName%", oldDivision.getShortName())
                    );

                    if (newDivision == this.minimumForRanked) {
                        Common.sendMMMessage(player, LanguageManager.getString("MATCH.DUEL.RANKED-REQUIREMENT-REACHED")
                                .replace("%newDivision_fullName%", newDivision.getFullName())
                                .replace("%newDivision_shortName%", newDivision.getShortName())
                        );
                    }
                }
            }
        }
    }

}
