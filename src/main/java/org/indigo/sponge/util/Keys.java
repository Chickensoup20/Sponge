package org.indigo.sponge.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class Keys {
    public static NamespacedKey ITEM_ID, DATA_VERSION;

    public static void init(Plugin plugin){
        ITEM_ID = new NamespacedKey(plugin,"item_id");
        DATA_VERSION = new NamespacedKey(plugin,"data_version");
    }
}
