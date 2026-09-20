package me.redst.lootingEXP.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LootingExpTabCompleter implements TabCompleter {

    private static final List<String> NONE = List.of();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        if (!sender.hasPermission(LootingExpCommand.PERMISSION)) {
            return NONE;
        }
        return switch (args.length) {
            case 1 -> match(args[0], CommandTree.SUBCOMMANDS);
            case 2 -> match(args[1], systemsFor(args[0]));
            case 3 -> match(args[2], settingsFor(args[0], args[1]));
            case 4 -> match(args[3], valuesFor(args[0], args[1], args[2]));
            case 5 -> match(args[4], roundingModesFor(args));
            default -> NONE;
        };
    }

    private static List<String> systemsFor(String subcommand) {
        return switch (subcommand.toLowerCase(Locale.ROOT)) {
            case CommandTree.SET, CommandTree.TOGGLE, CommandTree.GET -> CommandTree.SYSTEMS;
            default -> NONE;
        };
    }

    private static List<String> settingsFor(String subcommand, String system) {
        if (!CommandTree.MORE_EXP.equalsIgnoreCase(system)) {
            return NONE;
        }
        return switch (subcommand.toLowerCase(Locale.ROOT)) {
            case CommandTree.SET -> CommandTree.SETTABLE;
            case CommandTree.GET -> CommandTree.READABLE;
            default -> NONE;
        };
    }

    private static List<String> valuesFor(String subcommand, String system, String setting) {
        if (!CommandTree.MORE_EXP.equalsIgnoreCase(system)) {
            return NONE;
        }
        String key = setting.toLowerCase(Locale.ROOT);
        if (CommandTree.GET.equalsIgnoreCase(subcommand)) {
            return CommandTree.ROUNDING.equals(key) ? CommandTree.ROUNDING_KEYS : NONE;
        }
        if (!CommandTree.SET.equalsIgnoreCase(subcommand)) {
            return NONE;
        }
        return switch (key) {
            case CommandTree.PERCENT -> CommandTree.PERCENT_EXAMPLES;
            case CommandTree.MAX_XP -> CommandTree.MAX_XP_EXAMPLES;
            case CommandTree.ROUNDING -> CommandTree.ROUNDING_KEYS;
            default -> NONE;
        };
    }

    private static List<String> roundingModesFor(String[] args) {
        boolean isRoundingMode = CommandTree.SET.equalsIgnoreCase(args[0])
                && CommandTree.MORE_EXP.equalsIgnoreCase(args[1])
                && CommandTree.ROUNDING.equalsIgnoreCase(args[2])
                && CommandTree.MODE.equalsIgnoreCase(args[3]);
        return isRoundingMode ? CommandTree.ROUNDING_MODES : NONE;
    }

    private static List<String> match(String typed, List<String> options) {
        if (options.isEmpty()) {
            return NONE;
        }
        return StringUtil.copyPartialMatches(typed, options, new ArrayList<>(options.size()));
    }
}
