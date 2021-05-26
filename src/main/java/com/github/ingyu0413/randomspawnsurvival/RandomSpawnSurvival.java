package com.github.ingyu0413.randomspawnsurvival;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class RandomSpawnSurvival extends JavaPlugin implements Listener {
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
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public Location getSpawnLocation(String name) {
        int seed = name.hashCode();
        Random random = new Random(((long) seed) ^ 0x20050413);
        World world = Bukkit.getWorlds().get(0);
        WorldBorder worldborder = world.getWorldBorder();
        double bordersize = worldborder.getSize() * 0.7;
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
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        switch (e.getPlayerProfile().getId().toString()) {
            case "8bd662c7-53f2-4122-ae0c-5c43f8d362e8": //boringhoneycake
            case "64ff1c62-80a4-48ee-bb7e-14b1967eda9d": //gligu
            case "6a6db264-f33f-4d66-9165-8889cb756901": //KRnoobie
            case "ed8b9276-7c93-4149-b381-f6b81afa7583": //ningenbbane
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "io.netty.channel.AbstractChannel$AnnotatedConnectException: Connect refused: no further information:");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.teleport(getSpawnLocation(player.getName()));
        }
        player.setCompassTarget(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
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
        e.setNumPlaye rs(1);
        long gametime = Bukkit.getServer().getWorld("world").getTime() + 6000;
        String time = String.format("%02d:%02d:%02d", gametime / 1000, (int) (gametime % 1000 / 1000.0 * 60), (int) (gametime % 1000 / 1000.0 * 3));
        e.setMotd(ChatColor.BLUE + "" + ChatColor.BOLD + "RandomSpawnSurvival" + ChatColor.RESET + " " + ChatColor.DARK_GRAY + time);
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