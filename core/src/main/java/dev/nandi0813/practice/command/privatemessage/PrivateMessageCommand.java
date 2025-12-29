package dev.nandi0813.practice.command.privatemessage;

import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public abstract class PrivateMessageCommand extends BukkitCommand {

    public PrivateMessageCommand(String command, String[] aliases) {
        super(command);

        this.setAliases(Arrays.asList(aliases));

        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer());
            map.register(command, this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        executeCommand(commandSender, s, strings);
        return false;
    }

    public abstract void executeCommand(CommandSender sender, String label, String[] args);


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return onTabComplete(sender, args);
    }

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

}
