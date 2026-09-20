package me.redst.lootingEXP;

import me.redst.lootingEXP.command.LootingExpCommand;
import me.redst.lootingEXP.command.LootingExpTabCompleter;
import me.redst.lootingEXP.config.ConfigValidator;
import me.redst.lootingEXP.config.LootingExpConfig;
import me.redst.lootingEXP.config.ValidationReport;
import me.redst.lootingEXP.moreexp.MoreExpManager;
import me.redst.lootingEXP.moreexp.XpDropHandler;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class LootingEXP extends JavaPlugin {

    private static final String CONFIG_FILE = "config.yml";

    private final MoreExpManager moreExp = new MoreExpManager();

    private File configFile;
    private YamlConfiguration configuration = new YamlConfiguration();
    private boolean configUsable = true;

    @Override
    public void onEnable() {
        this.configFile = new File(getDataFolder(), CONFIG_FILE);
        if (!this.configFile.exists()) {
            saveResource(CONFIG_FILE, false);
        }

        reloadSettings();
        getServer().getPluginManager().registerEvents(new XpDropHandler(this.moreExp), this);
        registerCommand();

        getLogger().info("LootingEXP enabled.");
    }

    public ValidationReport reloadSettings() {
        ValidationReport report = new ValidationReport();
        YamlConfiguration loaded = new YamlConfiguration();
        this.configUsable = readInto(loaded);

        if (!this.configUsable) {
            report.unreadable();
            this.configuration = loaded;
            this.moreExp.apply(LootingExpConfig.defaults());
            return report;
        }

        LootingExpConfig settings = ConfigValidator.validate(loaded, report);
        if (report.needsRewrite()) {
            ConfigValidator.writeBack(loaded, settings, bundledDefaults());
            writeToDisk(loaded);
        }
        this.configuration = loaded;
        this.moreExp.apply(settings);
        report.logTo(getLogger());
        return report;
    }

    public boolean updateSetting(String path, Object value) {
        if (!this.configUsable) {
            return false;
        }
        this.configuration.set(path, value);
        if (!writeToDisk(this.configuration)) {
            return false;
        }
        this.moreExp.apply(ConfigValidator.validate(this.configuration, new ValidationReport()));
        return true;
    }

    public boolean isConfigUsable() {
        return this.configUsable;
    }

    private boolean readInto(YamlConfiguration target) {
        try {
            target.load(this.configFile);
            return true;
        } catch (FileNotFoundException ex) {
            return true;
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().severe("config.yml could not be read: " + ex.getMessage());
            getLogger().severe("Default settings are in use and the file has been left untouched. "
                    + "Fix it and run /lootingexp reload.");
            return false;
        }
    }

    private boolean writeToDisk(YamlConfiguration source) {
        try {
            source.save(this.configFile);
            return true;
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "config.yml could not be saved.", ex);
            return false;
        }
    }

    private ConfigurationSection bundledDefaults() {
        InputStream stream = getResource(CONFIG_FILE);
        if (stream == null) {
            return null;
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException ex) {
            return null;
        }
    }

    private void registerCommand() {
        PluginCommand command = getCommand("lootingexp");
        if (command == null) {
            getLogger().severe("The /lootingexp command is missing from plugin.yml.");
            return;
        }
        command.setExecutor(new LootingExpCommand(this, this.moreExp));
        command.setTabCompleter(new LootingExpTabCompleter());
    }
}
