package me.redst.lootingEXP.moreexp;

import me.redst.lootingEXP.config.LootingExpConfig;
import org.bukkit.damage.DamageSource;

public final class MoreExpManager {

    private volatile LootingExpConfig settings = LootingExpConfig.defaults();

    public LootingExpConfig settings() {
        return this.settings;
    }

    public void apply(LootingExpConfig settings) {
        this.settings = settings;
    }

    public int modifiedXp(LootingExpConfig settings, int baseXp, DamageSource source) {
        int lootingLevel = LootingResolver.resolve(source);
        if (lootingLevel <= 0) {
            return baseXp;
        }
        return LootingCalculator.apply(baseXp,
                lootingLevel,
                settings.percentHundredths(),
                settings.maxXpPerKill(),
                settings.roundingMode());
    }
}
