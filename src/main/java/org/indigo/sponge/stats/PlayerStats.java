package org.indigo.sponge.stats;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
    private final Map<Stat,Double> values = new HashMap<>();

    public void add(Stat stat, double amount){
        double current = values.getOrDefault(stat,0.0);
        values.put(stat,current+amount);
    }

    public double get(Stat stat){
        return values.getOrDefault(stat,0.0);
    }
}
