package me.redst.lootingEXP.moreexp;

import me.redst.lootingEXP.config.LootingExpConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class XpDropHandler implements Listener {

    private final MoreExpManager moreExp;

    public XpDropHandler(MoreExpManager moreExp) {
        this.moreExp = moreExp;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LootingExpConfig settings = this.moreExp.settings();
        if (!settings.isMoreExpActive()) {
            return;
        }
        if (event instanceof PlayerDeathEvent) {
            return;
        }

        int baseXp = event.getDroppedExp();
        if (baseXp <= 0) {
            return;
        }

        int finalXp = this.moreExp.modifiedXp(settings, baseXp, event.getDamageSource());
        if (finalXp != baseXp) {
            event.setDroppedExp(finalXp);
        }
    }
}
