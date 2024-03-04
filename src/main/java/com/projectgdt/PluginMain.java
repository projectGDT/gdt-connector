package com.projectgdt;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public final class PluginMain extends JavaPlugin {
    public Socket socket;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getInt("serverId") < 0) {
            System.err.println("Failed to enable the plugin. Please check /plugins/gdt-connect/config.yml");
            return;
        }

        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new LogoutListener(this), this);
        connectBackend();
    }

    @Override
    public void onDisable() {
        if (socket.connected()) {
            socket.disconnect();
        }
        socket.off();
        socket = null;
    }

    private void connectBackend() {
        try {
            // add auth data
            Map<String, String> auth = new HashMap<>();
            auth.put("id", String.valueOf(getConfig().getInt("serverId")));
            auth.put("token", getConfig().getString("token"));

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

            // try to connect
            socket.connect();
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    final private Emitter.Listener idInvalid = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            System.err.println("Failed to enable the plugin. Please check your serverId in /plugins/gdt-connect/config.yml");
            socket.disconnect();
        }
    };

    final private Emitter.Listener tokenInvalid = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            System.err.println("Failed to enable the plugin. Please check your token in /plugins/gdt-connect/config.yml");
            socket.disconnect();
        }
    };

    final private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... objects) {
            getLogger().info("connected");
        }
    };
}
