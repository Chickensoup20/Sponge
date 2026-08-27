package org.indigo.sponge;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class GameEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = new GamePlayer(player, GamePlayer.State.LOBBY);
        gamePlayer.applyState(GamePlayer.State.LOBBY);
        if (!Sponge.gameLoaded)
        {
            AttributeInstance att = player.getAttribute(Attribute.MOVEMENT_SPEED);
            att.setBaseValue(0);
            AttributeInstance att2 = player.getAttribute(Attribute.JUMP_STRENGTH);
            att2.setBaseValue(0);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.showTitle(Title.title(MiniMessage.miniMessage().deserialize("<white><b>GAME LOADING"), MiniMessage.miniMessage().deserialize("<gray><i>please wait..."),0,20,5));
                    player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(-1, 1));
                    if(Sponge.gameLoaded) {
                        player.showTitle(Title.title(MiniMessage.miniMessage().deserialize("<green>✔ <b>DONE <!b>✔"), MiniMessage.miniMessage().deserialize(""),2,20,5));
                        player.clearActivePotionEffects();
                        att.setBaseValue(player.getAttribute(Attribute.MOVEMENT_SPEED).getDefaultValue());
                        att2.setBaseValue(Attribute.JUMP_STRENGTH.getDefaultValue());
                        cancel();
                    }
                }
            }.runTaskTimer(Sponge.plugin, 1, 1);
        }


        if (!Sponge.joinedPlayers.contains(player))
        {
            Sponge.joinedPlayers.add(player);
            event.getPlayer().sendMessage("Created new file");
            player.addResourcePack(UUID.randomUUID(),"https://github.com/Kr4sty/Sponge_Resourcepack/raw/refs/heads/master/Sponge.zip",null,"Download me please",true);
            player.setCollidable(false);
            player.setAllowFlight(false);
        }
    }
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Sponge.gameLoaded = true;
        player.give(Sponge.itemDic.get("test"));
    }
}
