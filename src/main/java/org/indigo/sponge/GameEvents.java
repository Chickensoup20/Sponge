package org.indigo.sponge;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.indigo.sponge.functions.Item;
import org.indigo.sponge.game.Game;
import org.indigo.sponge.registries.Games;
import org.indigo.sponge.registries.Items;
import org.indigo.sponge.registries.Players;

import java.util.UUID;

public class GameEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SpongePlayer session = Players.find(player);
        if (session == null) {
            session = new SpongePlayer(player, SpongePlayer.State.LOBBY);
            Players.register(session);
        } else {
            session.attach(player);
        }
        session.applyState(SpongePlayer.State.LOBBY);

        Games.gameLoaded = true;

        player.addResourcePack(UUID.randomUUID(),"https://github.com/Kr4sty/Sponge_Resourcepack/raw/refs/heads/master/Sponge.zip",null,"Download me please",true);

        player.setCollidable(false);
        player.setAllowFlight(false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (Game game : Games.runningGames) {
            game.alivePlayers.remove(player);
        }
        Players.remove(player.getUniqueId());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        SpongePlayer session = Players.find(event.getPlayer());
        if (session == null || session.getState() != SpongePlayer.State.LOBBY) {
            return;
        }
        Player player = event.getPlayer();
        Games.gameLoaded = true;
        player.give(Items.itemDic.get("test"));
        player.give(Items.itemDic.get("trainingSword"));
        player.give(Items.itemDic.get("leatherHelmet"));
        player.give(Items.itemDic.get("razorBlade"));
        player.give(Items.itemDic.get("chickenLeg"));
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {
        ItemStack item = event.getItemDrop().getItemStack();
        UUID uuid = event.getItemDrop().getUniqueId();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(new NamespacedKey(Sponge.plugin, "id")))
        {
            event.setCancelled(true);
            return;
        }
        Item.itemGlow(uuid, item);
    }


}
