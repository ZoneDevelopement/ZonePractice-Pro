package dev.nandi0813.practice.manager.fight.belowname;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.Locale;
import java.util.Objects;

public class BelowNameManager {

    private static final boolean SATURATED_HEART_INDICATOR = ConfigManager.getBoolean("MATCH-SETTINGS.HEALTH-BELOW-NAME.SATURATED-HEART-INDICATOR");
    private static final boolean DECIMAL_ALWAYS_SHOW = ConfigManager.getBoolean("MATCH-SETTINGS.HEALTH-BELOW-NAME.DECIMAL-HEART-INDICATOR.ALWAYS-SHOW");
    private static final boolean LOW_HEALTH_RATIO = ConfigManager.getBoolean("MATCH-SETTINGS.HEALTH-BELOW-NAME.DECIMAL-HEART-INDICATOR.LOW-HEALTH-DECIMAL-RATIO");
    private static final double LOW_HEALTH_THRESHOLD = ConfigManager.getDouble("MATCH-SETTINGS.HEALTH-BELOW-NAME.LOW-HEALTH-THRESHOLD") * 2.0;
    private static final double CONFIG_SCALE = ConfigManager.getDouble("MATCH-SETTINGS.HEALTH-BELOW-NAME.SCALE");

private static BelowNameManager instance;

public static BelowNameManager getInstance() {
        if (instance == null) {
            instance = new BelowNameManager();
        }
        return instance;
    }

    private final String objectiveName = "ZPP_BELOW";
    private final Component displayName = Component.empty();

    private BelowNameManager() {
        this.initIndicators();
    }

    private final Runnable hpUpdate = () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective(objectiveName);

            if (objective == null) {
                continue;
            }

            for (Player otherPlayer : player.getWorld().getPlayers()) {
                double health = PlayerUtil.getPlayerHealth(otherPlayer);
                int hp = (int) Math.ceil(health);

                Score score = objective.getScore(otherPlayer.getName());
                score.setScore(hp);

                Component formattedHealth = formatHealth(otherPlayer, health);

                score.customName(null);
                score.numberFormat(NumberFormat.fixed(formattedHealth));
            }
        }
    };

    public void initIndicators() {
        Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), hpUpdate, 0, 5L);
    }

    public void initForUser(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(objectiveName, Criteria.DUMMY, displayName, RenderType.INTEGER);
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }

    public void cleanUpForUser(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective != null) {
            objective.unregister();
        }
    }

    private Component formatHealth(Player player, double health) {
        NamedTextColor heartColor = NamedTextColor.RED;
        if (SATURATED_HEART_INDICATOR && isSaturated(player)) {
            heartColor = NamedTextColor.YELLOW;
        }

        double scale = Math.clamp(CONFIG_SCALE == 0 ? 20.0 : CONFIG_SCALE, 10.0, 100.0);
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        double displayHealth = (health / maxHealth) * scale;

        if (DECIMAL_ALWAYS_SHOW || (LOW_HEALTH_RATIO && health < LOW_HEALTH_THRESHOLD)) {
            return Component.text(
                        String.format(Locale.US, "%.1f", displayHealth),
                        NamedTextColor.WHITE
                    ).append(Component.text("♥", heartColor));
        }

        return Component.text((int) Math.ceil(displayHealth) + " ", NamedTextColor.WHITE)
                .append(Component.text("♥", heartColor));
    }

    private boolean isSaturated(Player player) {
        return player.hasPotionEffect(PotionEffectType.SATURATION) || (player.getFoodLevel() >= 20 && player.getSaturation() > 0);
    }
}