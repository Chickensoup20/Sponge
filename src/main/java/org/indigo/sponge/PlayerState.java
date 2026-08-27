package org.indigo.sponge;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerState extends Sponge{
    private Player player;
    private State currentState;
    public enum State{
        LOBBY,
        INGAME,
        DEV
    }
    public PlayerState(Player player, State state){
        currentState = state;
        this.player = player;

    }
    public void applyState(){


    }
    public void applyState(State state){

    }
}
