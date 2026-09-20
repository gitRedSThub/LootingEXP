package me.redst.lootingEXP.command;

import me.redst.lootingEXP.config.RoundingMode;

import java.util.Arrays;
import java.util.List;

final class CommandTree {

    static final String SET = "set";
    static final String TOGGLE = "toggle";
    static final String GET = "get";
    static final String RELOAD = "reload";
    static final String HELP = "help";

    static final String MORE_EXP = "more-exp";

    static final String ENABLED = "enabled";
    static final String PERCENT = "exp-per-looting-level-percent";
    static final String MAX_XP = "max-xp-per-kill";
    static final String ROUNDING = "rounding";
    static final String MODE = "mode";

    static final List<String> SUBCOMMANDS = List.of(SET, TOGGLE, GET, RELOAD, HELP);
    static final List<String> SYSTEMS = List.of(MORE_EXP);

    static final List<String> SETTABLE = List.of(PERCENT, MAX_XP, ROUNDING);
    static final List<String> READABLE = List.of(ENABLED, PERCENT, MAX_XP, ROUNDING);
    static final List<String> ROUNDING_KEYS = List.of(MODE);

    static final List<String> ROUNDING_MODES =
            Arrays.stream(RoundingMode.values()).map(Enum::name).toList();

    static final List<String> PERCENT_EXAMPLES = List.of("10", "20", "30", "50", "100");
    static final List<String> MAX_XP_EXAMPLES = List.of("100", "500", "1000", "5000", "10000");

    private CommandTree() {
    }
}
