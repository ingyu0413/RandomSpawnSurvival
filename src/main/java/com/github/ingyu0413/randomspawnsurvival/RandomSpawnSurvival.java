package com.github.ingyu0413.randomspawnsurvival;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class RandomSpawnSurvival extends JavaPlugin {
    @Override
    public void onEnable() {
        List<World> worldlist = Bukkit.getWorlds();
        for (World world : worldlist) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            WorldBorder worldborder = world.getWorldBorder();
            worldborder.setCenter(new Location(world, 0.0, 0.0, 0.0));
            worldborder.setSize(16384.0);
            world.setSpawnLocation(world.getHighestBlockAt(0, 0).getLocation());
        }
        Bukkit.getScheduler().runTaskTimer(this, new Restarter(), 0L, 20L * 60L);
    }

    public Location getSpawnLocation(String name) {
        int seed = name.hashCode();
        Random random = new Random(((long) seed) ^ 0x20050413);
        World world = Bukkit.getWorlds().get(0);
        WorldBorder worldborder = world.getWorldBorder();
        double bordersize = worldborder.getSize();
        double x = random.nextDouble() * bordersize - bordersize / 2.0;
        double z = random.nextDouble() * bordersize - bordersize / 2.0;
        Block spawnblock = world.getHighestBlockAt((int)Math.floor(x), (int)Math.floor(z));
        return spawnblock.getLocation().add(0.5, 1.0, 0.5);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            e.allow();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.joinMessage(null);
        Player player = e.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.teleport(getSpawnLocation(player.getName()));
        }
        player.setCompassTarget(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.deathMessage(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (e.isBedSpawn() || e.isAnchorSpawn()) return;
        e.setRespawnLocation(getSpawnLocation(e.getPlayer().getName()));
    }

    @EventHandler
    public void onPaperServerListPing(PaperServerListPingEvent e) {
        e.setNumPlayers(1);
        e.setMotd(ChatColor.BLUE + "" + ChatColor.BOLD + "Random Spawn Survival Server");
        e.getPlayerSample().clear();
    }
}

class Restarter implements Runnable {
    public long starttime = System.currentTimeMillis();

    public void run() {
        long elapsedtime = System.currentTimeMillis() - starttime;
        long restarttime = 1000L * 60L * 60L * 2L;

        if (elapsedtime >= restarttime) {
            Bukkit.shutdown();
        } else if (elapsedtime >= restarttime - 60L * 1000L) {
            Bukkit.broadcastMessage("1분 뒤 서버가 재시작됩니다");
        }
    }
}