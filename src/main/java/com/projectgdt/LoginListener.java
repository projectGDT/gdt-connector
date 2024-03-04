package com.projectgdt;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public final class LoginListener implements Listener {
    private final PluginMain plugin;

    public LoginListener(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        String timestamp = String.valueOf(new Date().getTime());

        Map<String, Object> profile = new HashMap<>();
        if (plugin.getServer().getOnlineMode()) {
            profile.put("uniqueIdProvider", -1);
        }
        else {
            profile.put("uniqueIdProvider", plugin.getConfig().getInt("serverId"));
        }
        profile.put("uniqueId", event.getPlayer().getUniqueId());
        profile.put("cachedPlayerName", event.getPlayer().getName());

        Map<String, Object> data = new HashMap<>();
        data.put("profile", profile);
        data.put("timestamp", timestamp);

        plugin.socket.emit("player-login", data);
    }
}
