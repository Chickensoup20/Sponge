package org.indigo.sponge.item.bahavior;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HitContext {
    private final Player attacker;
    private final LivingEntity victim;
    private final ItemStack item;
    private double damage;
    private boolean cancelled;

    public HitContext(Player attacker, LivingEntity victim, ItemStack item, double damage, boolean cancelled) {
        this.attacker = attacker;
        this.victim = victim;
        this.item = item;
        this.damage = damage;
        this.cancelled = cancelled;
    }

    public Player getAttacker() {
        return attacker;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public ItemStack getItem() {
        return item;
    }

    public LivingEntity getVictim() {
        return victim;
    }
}
