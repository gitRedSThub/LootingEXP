package me.redst.lootingEXP.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public final class Msg {

    public static final TextColor BRAND = TextColor.color(0x4FD1FF);
    public static final TextColor LABEL = TextColor.color(0xB6C2CC);
    public static final TextColor VALUE = TextColor.color(0xFFFFFF);
    public static final TextColor TREE = TextColor.color(0x64727D);
    public static final TextColor GOOD = TextColor.color(0x66E08A);
    public static final TextColor BAD = TextColor.color(0xFF8080);
    public static final TextColor WARN = TextColor.color(0xFFD166);
    public static final TextColor HINT = TextColor.color(0x7C8A96);

    private static final Component PREFIX = Component.text()
            .append(Component.text("LootingEXP", BRAND, TextDecoration.BOLD))
            .append(Component.text(" » ", TREE))
            .build();

    private Msg() {
    }

    public static void prefixed(CommandSender to, Component body) {
        to.sendMessage(PREFIX.append(body));
    }

    public static void info(CommandSender to, String text) {
        prefixed(to, Component.text(text, LABEL));
    }

    public static void success(CommandSender to, String text) {
        prefixed(to, Component.text(text, GOOD));
    }

    public static void error(CommandSender to, String text) {
        prefixed(to, Component.text(text, BAD));
    }

    public static void plain(CommandSender to, Component line) {
        to.sendMessage(line);
    }

    public static void blank(CommandSender to) {
        to.sendMessage(Component.empty());
    }

    public static TextComponent pair(String label, String value, TextColor valueColor) {
        return Component.text()
                .append(Component.text(label, LABEL))
                .append(Component.text(": ", TREE))
                .append(Component.text(value, valueColor))
                .build();
    }
}
