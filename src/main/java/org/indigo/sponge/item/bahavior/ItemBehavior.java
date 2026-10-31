package org.indigo.sponge.item.bahavior;

public interface ItemBehavior {
    default void onHit(HitContext context) {};
    default void onUse(UseContext context) {};
}
