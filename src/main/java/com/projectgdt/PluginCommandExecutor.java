package com.projectgdt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PluginCommandExecutor implements CommandExecutor {
    private final PluginMain plugin;

    public PluginCommandExecutor(PluginMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // check type of the command sender
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be executed in the console.");
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("setconfig")) {
            // check number of arguments
            if (args.length != 2) {
                sender.sendMessage("Usage: /setconfig [serverId] [token]");
                return false;
            }

            // check type of arguments
            long serverId;
            try {
                serverId = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("ServerId must be numbers.");
                return false;
            }
            String token = args[1];

            // set config.yml
            plugin.getConfig().set("serverId", serverId);
            plugin.getConfig().set("token", token);

            plugin.saveConfig();

            // try to connect backend
            plugin.connectBackend(plugin.getConfig().getLong("serverId"), plugin.getConfig().getString("token"));
        }
        return false;
    }
}
