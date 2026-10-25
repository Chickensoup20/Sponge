package org.indigo.sponge.stats;

import net.kyori.adventure.text.Component;

public class HealthStat implements Stat{
    private double value;


    public HealthStat(double value){
        this.value = value;
    }

    @Override
    public void apply() {

    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public Component getName() {
        return null;
    }
}
