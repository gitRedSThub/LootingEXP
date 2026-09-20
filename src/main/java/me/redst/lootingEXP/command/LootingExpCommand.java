package me.redst.lootingEXP.command;

import me.redst.lootingEXP.LootingEXP;
import me.redst.lootingEXP.config.ConfigValidator;
import me.redst.lootingEXP.config.LootingExpConfig;
import me.redst.lootingEXP.config.NumberParser;
import me.redst.lootingEXP.config.RoundingMode;
import me.redst.lootingEXP.config.ValidationReport;
import me.redst.lootingEXP.moreexp.MoreExpManager;
import me.redst.lootingEXP.util.Format;
import me.redst.lootingEXP.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class LootingExpCommand implements CommandExecutor {

    public static final String PERMISSION = "lootingexp.admin";

    private static final String NAME_MORE_EXP = "MoreEXP";
    private static final String NAME_PERCENT = "XP Per Looting Level";
    private static final String NAME_MAX_XP = "Maximum XP Per Kill";
    private static final String NAME_ROUNDING = "Rounding";

    private final LootingEXP plugin;
    private final MoreExpManager moreExp;

    public LootingExpCommand(LootingEXP plugin, MoreExpManager moreExp) {
        this.plugin = plugin;
        this.moreExp = moreExp;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Msg.error(sender, "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showTree(sender);
            Msg.plain(sender, Component.text("Run /" + label + " help for the full command list.", Msg.HINT));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case CommandTree.SET -> handleSet(sender, label, args);
            case CommandTree.TOGGLE -> handleToggle(sender, label, args);
            case CommandTree.GET -> handleGet(sender, args);
            case CommandTree.RELOAD -> handleReload(sender);
            case CommandTree.HELP -> showHelp(sender, label);
            default -> {
                Msg.error(sender, "Unknown command: " + args[0]);
                Msg.plain(sender, Component.text("Run /" + label + " help to see what is available.", Msg.HINT));
            }
        }
        return true;
    }

    private void handleSet(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            usage(sender, "/" + label + " set " + CommandTree.MORE_EXP + " <setting> <value>");
            return;
        }
        if (!CommandTree.MORE_EXP.equalsIgnoreCase(args[1])) {
            unknownSystem(sender, args[1]);
            return;
        }
        if (args.length < 3) {
            usage(sender, "/" + label + " set " + CommandTree.MORE_EXP + " <setting> <value>");
            listSettings(sender);
            return;
        }

        switch (args[2].toLowerCase(Locale.ROOT)) {
            case CommandTree.ENABLED -> {
                Msg.error(sender, NAME_MORE_EXP + " is the main system, so it is not changed with set.");
                Msg.plain(sender, Component.text("Use /" + label + " toggle " + CommandTree.MORE_EXP
                        + " to switch it on or off.", Msg.HINT));
            }
            case CommandTree.PERCENT -> {
                if (args.length < 4) {
                    usage(sender, "/" + label + " set " + CommandTree.MORE_EXP + " " + CommandTree.PERCENT
                            + " <" + ConfigValidator.percentRange() + ">");
                    return;
                }
                setPercent(sender, args[3]);
            }
            case CommandTree.MAX_XP -> {
                if (args.length < 4) {
                    usage(sender, "/" + label + " set " + CommandTree.MORE_EXP + " " + CommandTree.MAX_XP
                            + " <" + ConfigValidator.maxXpRange() + ">");
                    return;
                }
                setMaxXp(sender, args[3]);
            }
            case CommandTree.ROUNDING -> {
                if (args.length >= 4 && !CommandTree.MODE.equalsIgnoreCase(args[3])) {
                    Msg.error(sender, "Unknown setting: " + args[3]);
                    Msg.plain(sender, Component.text("Available under " + CommandTree.ROUNDING + ": "
                            + CommandTree.MODE, Msg.HINT));
                    return;
                }
                if (args.length < 5) {
                    usage(sender, "/" + label + " set " + CommandTree.MORE_EXP + " " + CommandTree.ROUNDING
                            + " " + CommandTree.MODE + " <" + ConfigValidator.roundingChoices() + ">");
                    return;
                }
                setRounding(sender, args[4]);
            }
            default -> {
                Msg.error(sender, "Unknown setting: " + args[2]);
                listSettings(sender);
            }
        }
    }

    private void setPercent(CommandSender sender, String input) {
        NumberParser.Result parsed = NumberParser.parse(input);
        if (parsed.status() == NumberParser.Status.TOO_MANY_DECIMALS) {
            tooManyDecimals(sender);
            return;
        }
        if (!parsed.ok()) {
            notANumber(sender, input);
            return;
        }
        long hundredths = parsed.hundredths();
        if (hundredths < LootingExpConfig.PERCENT_MIN_HUNDREDTHS
                || hundredths > LootingExpConfig.PERCENT_MAX_HUNDREDTHS) {
            outsideRange(sender, ConfigValidator.percentRange());
            return;
        }
        if (save(sender, LootingExpConfig.PATH_PERCENT, ConfigValidator.asStoredNumber(hundredths))) {
            Msg.success(sender, NAME_PERCENT + " is now " + Format.decimal(hundredths) + "%.");
            describeBonus(sender, hundredths);
        }
    }

    private void setMaxXp(CommandSender sender, String input) {
        NumberParser.Result parsed = NumberParser.parse(input);
        if (parsed.status() == NumberParser.Status.TOO_MANY_DECIMALS) {
            tooManyDecimals(sender);
            return;
        }
        if (!parsed.ok()) {
            notANumber(sender, input);
            return;
        }
        long hundredths = parsed.hundredths();
        if (hundredths % 100L != 0L) {
            Msg.error(sender, "Invalid number.");
            Msg.plain(sender, Component.text("Experience is counted in whole points, so this value "
                    + "cannot have decimals.", Msg.HINT));
            return;
        }
        long whole = hundredths / 100L;
        if (whole < LootingExpConfig.MAX_XP_MIN || whole > LootingExpConfig.MAX_XP_MAX) {
            outsideRange(sender, ConfigValidator.maxXpRange());
            return;
        }
        if (save(sender, LootingExpConfig.PATH_MAX_XP, (int) whole)) {
            Msg.success(sender, NAME_MAX_XP + " is now " + Format.grouped(whole) + ".");
        }
    }

    private void setRounding(CommandSender sender, String input) {
        RoundingMode mode = RoundingMode.parse(input);
        if (mode == null) {
            Msg.error(sender, "Unknown rounding mode: " + input);
            Msg.plain(sender, Component.text("Available options: " + ConfigValidator.roundingChoices(), Msg.HINT));
            return;
        }
        if (save(sender, LootingExpConfig.PATH_ROUNDING_MODE, mode.name())) {
            Msg.success(sender, NAME_ROUNDING + " is now " + mode.displayName() + ".");
        }
    }

    private void handleToggle(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            usage(sender, "/" + label + " toggle " + CommandTree.MORE_EXP);
            return;
        }
        if (!CommandTree.MORE_EXP.equalsIgnoreCase(args[1])) {
            unknownSystem(sender, args[1]);
            return;
        }
        boolean next = !this.moreExp.settings().isMoreExpEnabled();
        if (save(sender, LootingExpConfig.PATH_ENABLED, next)) {
            Msg.prefixed(sender, Component.text()
                    .append(Component.text(NAME_MORE_EXP + " is now ", Msg.LABEL))
                    .append(onOff(next))
                    .append(Component.text(".", Msg.LABEL))
                    .build());
            if (next && this.moreExp.settings().percentHundredths() == 0L) {
                Msg.plain(sender, Component.text(NAME_PERCENT + " is 0.00%, so no extra XP will be given yet.",
                        Msg.HINT));
            }
        }
    }

    private void handleGet(CommandSender sender, String[] args) {
        if (args.length < 2) {
            showTree(sender);
            return;
        }
        if (!CommandTree.MORE_EXP.equalsIgnoreCase(args[1])) {
            unknownSystem(sender, args[1]);
            return;
        }
        LootingExpConfig settings = this.moreExp.settings();
        if (args.length < 3) {
            showMoreExpBranch(sender, settings);
            return;
        }

        switch (args[2].toLowerCase(Locale.ROOT)) {
            case CommandTree.ENABLED -> Msg.prefixed(sender, Component.text()
                    .append(Component.text(NAME_MORE_EXP + ": ", Msg.LABEL))
                    .append(onOff(settings.isMoreExpEnabled()))
                    .build());
            case CommandTree.PERCENT -> Msg.prefixed(sender,
                    Msg.pair(NAME_PERCENT, Format.decimal(settings.percentHundredths()) + "%", Msg.VALUE));
            case CommandTree.MAX_XP -> Msg.prefixed(sender,
                    Msg.pair(NAME_MAX_XP, Format.grouped(settings.maxXpPerKill()), Msg.VALUE));
            case CommandTree.ROUNDING -> {
                if (args.length >= 4 && !CommandTree.MODE.equalsIgnoreCase(args[3])) {
                    Msg.error(sender, "Unknown setting: " + args[3]);
                    Msg.plain(sender, Component.text("Available under " + CommandTree.ROUNDING + ": "
                            + CommandTree.MODE, Msg.HINT));
                    return;
                }
                Msg.prefixed(sender, Msg.pair(NAME_ROUNDING, settings.roundingMode().displayName(), Msg.VALUE));
            }
            default -> {
                Msg.error(sender, "Unknown setting: " + args[2]);
                Msg.plain(sender, Component.text("Available: " + String.join(", ", CommandTree.READABLE), Msg.HINT));
            }
        }
    }

    private void showTree(CommandSender sender) {
        LootingExpConfig settings = this.moreExp.settings();

        Msg.blank(sender);
        Msg.plain(sender, Component.text()
                .append(Component.text("LootingEXP", Msg.BRAND, TextDecoration.BOLD))
                .append(Component.text(" v" + this.plugin.getPluginMeta().getVersion(), Msg.HINT))
                .build());
        Msg.plain(sender, Component.text("│", Msg.TREE));

        Msg.plain(sender, branch("├─ ", Component.text()
                .append(Component.text(NAME_MORE_EXP + ": ", Msg.LABEL))
                .append(onOff(settings.isMoreExpEnabled()))
                .build()));
        Msg.plain(sender, branch("│  ├─ ",
                Msg.pair(NAME_PERCENT, Format.decimal(settings.percentHundredths()) + "%", Msg.VALUE)));
        Msg.plain(sender, branch("│  ├─ ",
                Msg.pair(NAME_MAX_XP, Format.grouped(settings.maxXpPerKill()), Msg.VALUE)));
        Msg.plain(sender, branch("│  └─ ",
                Msg.pair(NAME_ROUNDING, settings.roundingMode().displayName(), Msg.VALUE)));

        Msg.plain(sender, Component.text("│", Msg.TREE));
        Msg.plain(sender, branch("└─ ", Component.text("Status", Msg.LABEL)));
        Msg.plain(sender, branch("   └─ ", Component.text()
                .append(Component.text("XP bonus: ", Msg.LABEL))
                .append(statusOf(settings))
                .build()));
        Msg.blank(sender);
    }

    private void showMoreExpBranch(CommandSender sender, LootingExpConfig settings) {
        Msg.plain(sender, Component.text()
                .append(Component.text(NAME_MORE_EXP + ": ", Msg.LABEL))
                .append(onOff(settings.isMoreExpEnabled()))
                .build());
        Msg.plain(sender, branch("├─ ",
                Msg.pair(NAME_PERCENT, Format.decimal(settings.percentHundredths()) + "%", Msg.VALUE)));
        Msg.plain(sender, branch("├─ ",
                Msg.pair(NAME_MAX_XP, Format.grouped(settings.maxXpPerKill()), Msg.VALUE)));
        Msg.plain(sender, branch("└─ ",
                Msg.pair(NAME_ROUNDING, settings.roundingMode().displayName(), Msg.VALUE)));
    }

    private void handleReload(CommandSender sender) {
        ValidationReport report = this.plugin.reloadSettings();
        if (report.isUnreadable()) {
            Msg.error(sender, "config.yml could not be read, so the default settings are in use.");
            Msg.plain(sender, Component.text("The file has been left as it is. The console explains "
                    + "what is wrong with it.", Msg.HINT));
            return;
        }
        int invalid = report.invalidValues();
        if (invalid == 0) {
            Msg.success(sender, "Configuration reloaded successfully.");
            return;
        }
        Msg.info(sender, "Configuration reloaded.");
        Msg.plain(sender, Component.text(invalid == 1
                ? "1 invalid value was reset to its default."
                : invalid + " invalid values were reset to their defaults.", Msg.WARN));
    }

    private void showHelp(CommandSender sender, String label) {
        Msg.blank(sender);
        Msg.plain(sender, Component.text()
                .append(Component.text("LootingEXP", Msg.BRAND, TextDecoration.BOLD))
                .append(Component.text(" commands", Msg.LABEL))
                .build());
        helpLine(sender, "/" + label + " set", " <system> <setting> <value>", "Change a configurable value.");
        helpLine(sender, "/" + label + " toggle", " " + CommandTree.MORE_EXP, "Turn the main system on or off.");
        helpLine(sender, "/" + label + " get", " [system] [setting]", "Show current settings and status.");
        helpLine(sender, "/" + label + " reload", "", "Reload config.yml.");
        helpLine(sender, "/" + label + " help", "", "Show this list.");
        Msg.plain(sender, Component.text("Alias: /lexp", Msg.HINT));
        Msg.blank(sender);
    }

    private static void helpLine(CommandSender sender, String command, String arguments, String description) {
        Msg.plain(sender, Component.text()
                .append(Component.text("  " + command, Msg.BRAND))
                .append(Component.text(arguments, Msg.HINT))
                .append(Component.text(" - ", Msg.TREE))
                .append(Component.text(description, Msg.LABEL))
                .build()
                .clickEvent(ClickEvent.suggestCommand(command + (arguments.isEmpty() ? "" : " ")))
                .hoverEvent(HoverEvent.showText(Component.text("Click to type this command", Msg.HINT))));
    }

    private boolean save(CommandSender sender, String path, Object value) {
        if (!this.plugin.isConfigUsable()) {
            Msg.error(sender, "config.yml could not be read, so settings cannot be changed.");
            Msg.plain(sender, Component.text("Fix the file, then run /lootingexp reload.", Msg.HINT));
            return false;
        }
        if (this.plugin.updateSetting(path, value)) {
            return true;
        }
        Msg.error(sender, "Could not save config.yml. The server console has the details.");
        return false;
    }

    private static Component branch(String prefix, Component body) {
        return Component.text().append(Component.text(prefix, Msg.TREE)).append(body).build();
    }

    private static Component onOff(boolean enabled) {
        return Component.text(enabled ? "ON" : "OFF", enabled ? Msg.GOOD : Msg.BAD, TextDecoration.BOLD);
    }

    private static Component statusOf(LootingExpConfig settings) {
        if (!settings.isMoreExpEnabled()) {
            return Component.text("Off", Msg.BAD);
        }
        if (settings.percentHundredths() == 0L) {
            return Component.text("Idle (bonus is 0.00%)", Msg.WARN);
        }
        return Component.text("Active", Msg.GOOD);
    }

    private static void describeBonus(CommandSender sender, long hundredths) {
        if (hundredths == 0L) {
            Msg.plain(sender, Component.text("Looting will no longer give extra XP.", Msg.HINT));
            return;
        }
        long threeLevels = 10_000L + 3L * hundredths;
        Msg.plain(sender, Component.text("Looting III now drops "
                + Format.decimal(threeLevels) + "% of a mob's normal XP.", Msg.HINT));
    }

    private static void usage(CommandSender sender, String usage) {
        Msg.prefixed(sender, Component.text()
                .append(Component.text("Usage: ", Msg.LABEL))
                .append(Component.text(usage, Msg.VALUE))
                .build());
    }

    private static void unknownSystem(CommandSender sender, String given) {
        Msg.error(sender, "Unknown system: " + given);
        Msg.plain(sender, Component.text("Available: " + String.join(", ", CommandTree.SYSTEMS), Msg.HINT));
    }

    private static void listSettings(CommandSender sender) {
        Msg.plain(sender, Component.text("Settings: " + String.join(", ", CommandTree.SETTABLE), Msg.HINT));
    }

    private static void tooManyDecimals(CommandSender sender) {
        Msg.error(sender, "Invalid number.");
        Msg.plain(sender, Component.text("Values can contain at most "
                + NumberParser.MAX_DECIMAL_PLACES + " decimal places.", Msg.HINT));
    }

    private static void notANumber(CommandSender sender, String input) {
        Msg.error(sender, "Invalid number: " + input);
        Msg.plain(sender, Component.text("Enter a number, for example 30 or 30.00.", Msg.HINT));
    }

    private static void outsideRange(CommandSender sender, String range) {
        Msg.error(sender, "That value is outside the safe limit.");
        Msg.plain(sender, Component.text()
                .append(Component.text("Allowed range: ", Msg.HINT))
                .append(Component.text(range, Msg.VALUE))
                .build());
    }
}
