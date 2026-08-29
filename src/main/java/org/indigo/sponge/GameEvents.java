package org.indigo.sponge;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class GameEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SpongePlayer spongePlayer = new SpongePlayer(player, SpongePlayer.State.LOBBY);
        spongePlayer.applyState(SpongePlayer.State.LOBBY);
//        if (!Sponge.gameLoaded)
//        {
//            AttributeInstance att = player.getAttribute(Attribute.MOVEMENT_SPEED);
//            att.setBaseValue(0);
//            AttributeInstance att2 = player.getAttribute(Attribute.JUMP_STRENGTH);
//            att2.setBaseValue(0);
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    player.showTitle(Title.title(MiniMessage.miniMessage().deserialize("<white><b>GAME LOADING"), MiniMessage.miniMessage().deserialize("<gray><i>please wait..."),0,20,5));
//                    player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(-1, 1));
//                    if(Sponge.gameLoaded) {
//                        player.showTitle(Title.title(MiniMessage.miniMessage().deserialize("<green>✔ <b>DONE <!b>✔"), MiniMessage.miniMessage().deserialize(""),2,20,5));
//                        player.clearActivePotionEffects();
//                        att.setBaseValue(0.1);
//                        att2.setBaseValue(Attribute.JUMP_STRENGTH.getDefaultValue());
//                        cancel();
//                    }
//                }
//            }.runTaskTimer(Sponge.plugin, 1, 1);
//        }
        Sponge.gameLoaded = true;

        player.addResourcePack(UUID.randomUUID(),"https://github.com/Kr4sty/Sponge_Resourcepack/raw/refs/heads/master/Sponge.zip",null,"Download me please",true);

        if (!Sponge.joinedPlayers.contains(player))
        {
            Sponge.joinedPlayers.add(player);
            player.setCollidable(false);
            player.setAllowFlight(false);
        }
    }
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if(Sponge.playerStates.get(event.getPlayer()).getState() == SpongePlayer.State.LOBBY) {
            Player player = event.getPlayer();
            Sponge.gameLoaded = true;
            player.give(Sponge.itemDic.get("test"));
            player.give(Sponge.itemDic.get("trainingSword"));
            player.give(Sponge.itemDic.get("leatherHelmet"));
            player.give(Sponge.itemDic.get("razorBlade"));
            player.give(Sponge.itemDic.get("chickenLeg"));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {

    }


}
