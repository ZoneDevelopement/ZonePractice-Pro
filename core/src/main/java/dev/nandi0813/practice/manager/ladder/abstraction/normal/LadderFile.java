package dev.nandi0813.practice.manager.ladder.abstraction.normal;

import dev.nandi0813.practice.manager.backend.ConfigFile;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.nandi0813.practice.manager.ladder.enums.WeightClassType;
import dev.nandi0813.practice.manager.ladder.util.LadderFileUtil;
import dev.nandi0813.practice.util.BasicItem;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemSerializationUtil;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LadderFile extends ConfigFile {

    private final NormalLadder ladder;

    public LadderFile(NormalLadder ladder) {
        super("/ladders/", ladder.getName().toLowerCase());
        this.ladder = ladder;

        saveFile();
        reloadFile();
    }

    @Override
    public void setData() {
        config.set("name", ladder.getName());
        config.set("enabled", ladder.isEnabled());
        config.set("type", ladder.getType().toString());

        config.set("settings.regen", ladder.isRegen());
        config.set("settings.hunger", ladder.isHunger());
        config.set("settings.weightClass", ladder.getWeightClass().toString());
        config.set("settings.editable", ladder.isEditable());
        config.set("settings.drop-inventory", ladder.isDropInventoryPartyGames());
        config.set("settings.multiRoundStartCountdown", ladder.isMultiRoundStartCountdown());
        config.set("settings.hitdelay", ladder.getHitDelay());
        config.set("settings.rounds", ladder.getRounds());
        config.set("settings.maxduration", ladder.getMaxDuration());
        config.set("settings.epcooldown", ladder.getEnderPearlCooldown());
        config.set("settings.gacooldown", ladder.getGoldenAppleCooldown());
        config.set("settings.fireworkcooldown", ladder.getFireworkRocketCooldown());
        config.set("settings.startcountdown", ladder.getStartCountdown());
        config.set("settings.startmove", ladder.isStartMove());
        config.set("settings.matchtypes", LadderFileUtil.getMatchTypeNames(ladder.getMatchTypes()));
        config.set("settings.knockback", ladder.getLadderKnockback().get());
        config.set("settings.tntfusetime", ladder.getTntFuseTime());
        config.set("settings.healthbelowname", ladder.isHealthBelowName());
        config.set("settings.resetbuildafterround", ladder.isResetBuildAfterRound());
        config.set("settings.breakallblocks", ladder.isBreakAllBlocks());

        if (ladder instanceof RespawnableLadder respawnableLadder) {
            config.set("settings.respawntime", respawnableLadder.getRespawnTime());
        }

        if (ladder instanceof TempBuild tempBuild) {
            config.set("settings.tempbuild-delay", tempBuild.getBuildDelay());
        }

        if (ladder.getIcon() != null)
            config.set("icon", ladder.getIcon());
        else
            config.set("icon", null);

        if (ladder.getKitData() != null)
            ladder.getKitData().saveData(config, null);

        if (ladder.isBuild()) {
            List<String> destroyableBlocks = new ArrayList<>();
            for (BasicItem destroyableBlock : ladder.getDestroyableBlocks()) {
                if (destroyableBlock.getDamage() == 0)
                    destroyableBlocks.add(destroyableBlock.getMaterial().name());
                else
                    destroyableBlocks.add(destroyableBlock.getMaterial().name() + "::" + destroyableBlock.getDamage());
            }

            if (destroyableBlocks.isEmpty())
                config.set("destroyable-blocks", null);
            else
                config.set("destroyable-blocks", destroyableBlocks);
        }

        if (ladder.getCustomKitExtraItems().get(false) != null)
            config.set("custom-kit-extra-items.unranked", ItemSerializationUtil.itemStackArrayToBase64(ladder.getCustomKitExtraItems().get(false)));
        else
            config.set("custom-kit-extra-items.unranked", null);

        if (ladder.getCustomKitExtraItems().get(true) != null)
            config.set("custom-kit-extra-items.ranked", ItemSerializationUtil.itemStackArrayToBase64(ladder.getCustomKitExtraItems().get(true)));
        else
            config.set("custom-kit-extra-items.ranked", null);

        if (ladder instanceof CustomConfig)
            ((CustomConfig) ladder).setCustomConfig(config);

        saveFile();
    }

    @Override
    public void getData() {
        if (config.isBoolean("enabled")) {
            ladder.setEnabled(config.getBoolean("enabled"));
        }

        if (config.isBoolean("settings.regen")) {
            ladder.setRegen(config.getBoolean("settings.regen"));
        }

        if (config.isBoolean("settings.hunger")) {
            ladder.setHunger(config.getBoolean("settings.hunger"));
        }

        if (config.isBoolean("settings.ranked")) {
            ladder.setWeightClass(config.getBoolean("settings.ranked") ? WeightClassType.UNRANKED_AND_RANKED : WeightClassType.UNRANKED);
        }

        if (config.isString("settings.weightClass")) {
            ladder.setWeightClass(WeightClassType.valueOf(config.getString("settings.weightClass")));
        }

        if (config.isBoolean("settings.editable")) {
            ladder.setEditable(config.getBoolean("settings.editable"));
        }

        if (config.isBoolean("settings.drop-inventory")) {
            ladder.setDropInventoryPartyGames(config.getBoolean("settings.drop-inventory"));
        }

        if (config.isBoolean("settings.multiRoundStartCountdown")) {
            ladder.setMultiRoundStartCountdown(config.getBoolean("settings.multiRoundStartCountdown"));
        }

        if (config.isBoolean("settings.startmove")) {
            ladder.setStartMove(config.getBoolean("settings.startmove"));
        }

        if (config.isBoolean("settings.healthbelowname")) {
            ladder.setHealthBelowName(config.getBoolean("settings.healthbelowname"));
        }

        if (config.isBoolean("settings.resetbuildafterround")) {
            ladder.setResetBuildAfterRound(config.getBoolean("settings.resetbuildafterround"));
        }

        if (config.isBoolean("settings.breakallblocks")) {
            ladder.setBreakAllBlocks(config.getBoolean("settings.breakallblocks"));
        }

        if (config.isInt("settings.hitdelay")) {
            int hitDelay = config.getInt("settings.hitdelay");
            if (hitDelay < 0 || hitDelay > 100) hitDelay = 20;
            ladder.setHitDelay(hitDelay);
        } else
            ladder.setHitDelay(20);

        if (config.isInt("settings.rounds")) {
            int rounds = config.getInt("settings.rounds");
            if (rounds < 1 || rounds > 5) rounds = 1;
            ladder.setRounds(rounds);
        } else
            ladder.setRounds(1);

        if (config.isInt("settings.maxduration")) {
            ladder.setMaxDuration(config.getInt("settings.maxduration"));
        } else
            ladder.setMaxDuration(600);

        if (config.isInt("settings.epcooldown")) {
            int epCooldown = config.getInt("settings.epcooldown");
            if (epCooldown < 0 || epCooldown > 60) epCooldown = 0;
            ladder.setEnderPearlCooldown(epCooldown);
        }

        if (config.isInt("settings.gacooldown")) {
            int gaCooldown = config.getInt("settings.gacooldown");
            if (gaCooldown < 0 || gaCooldown > 30) gaCooldown = 0;
            ladder.setGoldenAppleCooldown(gaCooldown);
        }

        if (config.isInt("settings.fireworkcooldown")) {
            int fireworkCooldown = config.getInt("settings.fireworkcooldown");
            if (fireworkCooldown < 0 || fireworkCooldown > 30) fireworkCooldown = 1;
            ladder.setFireworkRocketCooldown(fireworkCooldown);
        }

        if (config.isInt("settings.startcountdown")) {
            int startCountdown = config.getInt("settings.startcountdown");
            if (startCountdown < 2 || startCountdown > 5) startCountdown = 3;
            ladder.setStartCountdown(startCountdown);
        } else
            ladder.setStartCountdown(3);

        if (config.isInt("settings.tntfusetime")) {
            int tntFuseTime = config.getInt("settings.tntfusetime");
            if (tntFuseTime < 1 || tntFuseTime > 10) tntFuseTime = 4;
            ladder.setTntFuseTime(tntFuseTime);
        } else
            ladder.setTntFuseTime(4);

        if (ladder instanceof RespawnableLadder respawnableLadder) {
            if (config.isInt("settings.respawntime")) {
                int respawnTime = config.getInt("settings.respawntime");
                if (respawnTime < 0 || respawnTime > 10) respawnTime = 3;
                respawnableLadder.setRespawnTime(respawnTime);
            } else
                respawnableLadder.setRespawnTime(3);
        }

        if (ladder instanceof TempBuild tempBuild) {
            if (config.isInt("settings.tempbuild-delay")) {
                int buildDelay = config.getInt("settings.tempbuild-delay");
                if (buildDelay < 3 || buildDelay > 30) buildDelay = 6;
                tempBuild.setBuildDelay(buildDelay);
            } else
                tempBuild.setBuildDelay(6);
        }

        if (config.isString("settings.knockback"))
            ladder.getLadderKnockback().get(config.getString("settings.knockback"));

        if (config.isList("settings.matchtypes"))
            LadderFileUtil.getLadderMatchTypes(ladder.getMatchTypes(), config.getStringList("settings.matchtypes"));

        ladder.getKitData().getData(config, null);

        if (ladder.isBuild() && config.isList("destroyable-blocks")) {
            List<String> dbList = config.getStringList("destroyable-blocks");
            for (String destroyableBlock : dbList) {
                try {
                    if (destroyableBlock.contains("::")) {
                        String[] s2 = destroyableBlock.split("::");
                        ladder.getDestroyableBlocks().add(new BasicItem(Material.valueOf(s2[0]), Short.parseShort(s2[1])));
                    } else {
                        ladder.getDestroyableBlocks().add(new BasicItem(Material.valueOf(destroyableBlock), (short) 0));
                    }
                } catch (IllegalArgumentException e) {
                    Common.sendConsoleMMMessage("<red>Incorrectly formatted destroyable block: " + destroyableBlock);
                }
            }
        }

        if (config.isItemStack("icon"))
            ladder.setIcon(config.getItemStack("icon"));
        if (config.isString("custom-kit-extra-items.unranked"))
            ladder.getCustomKitExtraItems().put(false, ItemSerializationUtil.itemStackArrayFromBase64(config.getString("custom-kit-extra-items.unranked")));
        if (config.isString("custom-kit-extra-items.ranked"))
            ladder.getCustomKitExtraItems().put(true, ItemSerializationUtil.itemStackArrayFromBase64(config.getString("custom-kit-extra-items.ranked")));

        if (ladder instanceof CustomConfig)
            ((CustomConfig) ladder).getCustomConfig(config);

        if (ladder.isEnabled() && !ladder.isReadyToEnable())
            ladder.setEnabled(false);
    }

}
