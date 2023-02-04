package riding.riding;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class Riding extends JavaPlugin implements Listener, CommandExecutor {
    HashMap<UUID,Boolean> isplayermounting = new HashMap<UUID,Boolean>();
    HashMap<UUID,Entity> playerriding = new HashMap<UUID,Entity>();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        for(Player player:Bukkit.getOnlinePlayers())
        {
            player.teleport(player.getLocation());
            isplayermounting.put(player.getUniqueId(), false);
        }
    }

    @Override
    public void onDisable() {

    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        return true;
    }

    @EventHandler
    public void onquit(PlayerQuitEvent e)
    {
        down(e.getPlayer());
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        isplayermounting.put(e.getPlayer().getUniqueId(),false);
    }
    @EventHandler
    public void onPlayerpassivedown(PlayerToggleSneakEvent e)
    {
        down(e.getPlayer());
    }
    @EventHandler
    public void onPlayeritemwasteEvent(PlayerDropItemEvent e)
    {
        e.setCancelled(true);
        Player player = e.getPlayer();
        if(isplayermounting.get(player.getUniqueId()))
        {

            down(player);

        }
        else if(!player.isSneaking())
        {
            isplayermounting.put(player.getUniqueId(),true);

            Entity entity = Bukkit.getWorld("world").spawnEntity(player.getLocation(), EntityType.HORSE);
            
            playerriding.put(player.getUniqueId(),entity);
            entity.setPassenger(player);

        }

    }
    public void down(Player player)
    {
        if(isplayermounting.get(player.getUniqueId()))
        {
            isplayermounting.put(player.getUniqueId(),false);

            playerriding.get(player.getUniqueId()).remove();
        }
    }
}
