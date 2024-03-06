package com.projectgdt;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class PluginMain extends JavaPlugin {
    public Socket socket;

    @Override
    public void onEnable() {
        // register command
        this.getCommand("setconfig").setExecutor(new PluginCommandExecutor(this));

        // register listeners
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new LogoutListener(this), this);

        // if it's the first launch, save default config.yml
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            // create initial config.yml
            saveDefaultConfig();
            System.out.println("The plugin has been enabled successfully!");
            System.out.println("Use command /setconfig to set your serverId and token, and the plugin will connect to backend automatically.");
            System.out.println("Usage: /setconfig [serverId] [token]");
        }

        else {
            // check data in config.yml
            long serverId = getConfig().getLong("serverId", -1);
            if (serverId == -1) {
                System.err.println("Invalid or empty serverId. Use command /setconfig to set serverId.");
                System.out.println("Usage: /setconfig [serverId] [token]");
                return;
            }
            String token = getConfig().getString("token", "");
            if (token.isEmpty()) {
                System.err.println("Invalid or empty token. Use command /setconfig to set token.");
                System.out.println("Usage: /setconfig [serverId] [token]");
                return;
            }
            // try to connect backend
            connectBackend(serverId, token);
        }
    }

    @Override
    public void onDisable() {
        if (socket.connected()) {
            socket.disconnect();
        }
        socket.off();
        socket = null;
    }

    public void connectBackend(long serverId, String token) {
        try {
            // add auth data
            Map<String, String> auth = new HashMap<>();
            auth.put("id", String.valueOf(serverId));
            auth.put("token", token);

            // create socket with auth data
            IO.Options opts = new IO.Options();
            opts.auth = auth;
            String backendAddress = getConfig().getString("backendAddress");
            socket = IO.socket("http://" + backendAddress + "/plugin", opts);

            // register listeners
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    System.err.println("Failed to connect to the server: " + args[0]);
                }
            });

            socket.on("id-invalid", idInvalid);
            socket.on("token-invalid", tokenInvalid);

            socket.on("kick-online-player", kickOnlinePlayer);

            // try to connect
            socket.connect();
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    final private Emitter.Listener idInvalid = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            System.err.println("Invalid or empty serverId. Use command /setconfig to set serverId.");
            System.out.println("Usage: /setconfig [serverId] [token]");
            socket.disconnect();
        }
    };

    final private Emitter.Listener tokenInvalid = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            System.err.println("Invalid or empty token. Use command /setconfig to set token.");
            System.out.println("Usage: /setconfig [serverId] [token]");
            socket.disconnect();
        }
    };

    final private Emitter.Listener kickOnlinePlayer = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            JSONObject data = (JSONObject) objects[0];
            String uuid = data.getJSONObject("profile").getString("uniqueId");

            Map<String, Object> responseData = new HashMap<>();
            Player kickedPlayer = getServer().getPlayer(uuid);
            if (kickedPlayer == null) {
                responseData.put("success", false);
            }
            else {
                kickedPlayer.kickPlayer("");
                responseData.put("success", true);
            }
            responseData.put("timestamp", new Date().getTime());
            socket.emit("kick-online-player-response", responseData);
        }
    };

    final private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            System.out.println("Trying to connect to backend...");
        }
    };
}
