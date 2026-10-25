package org.indigo.sponge.stats;

import net.kyori.adventure.text.Component;

public interface Stat {
    public void apply();
    public String getId();
    public Component getName();
}
