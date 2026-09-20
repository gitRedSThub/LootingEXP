package me.redst.lootingEXP.moreexp;

import org.bukkit.damage.DamageSource;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class LootingResolver {

    private static final Enchantment LOOTING = Enchantment.LOOTING;

    private LootingResolver() {
    }

    public static int resolve(DamageSource source) {
        if (!(source.getCausingEntity() instanceof Player player)) {
            return 0;
        }

        Entity direct = source.getDirectEntity();
        if (direct == null) {
            return 0;
        }
        if (direct == player || direct.equals(player)) {
            return levelOf(player.getInventory().getItemInMainHand());
        }
        if (direct instanceof AbstractArrow arrow) {
            return levelOf(arrow.getWeapon());
        }
        return 0;
    }

    private static int levelOf(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            return 0;
        }
        int level = weapon.getEnchantmentLevel(LOOTING);
        return Math.max(level, 0);
    }
}
