package net.johnbrooks.mh;

import net.johnbrooks.mh.managers.UpdateManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("MobCapture.Admin")) {
            sender.sendMessage(Language.PREFIX + "You do not have permission to run this command.");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage(Language.PREFIX + "This plugin was created by WiseHollow!");
                Player player = (Player) sender;
                TextComponent message = new TextComponent(Language.PREFIX + ChatColor.UNDERLINE + "Click here " +
                        ChatColor.RESET + "" + ChatColor.BLUE + "to see my profile and my other plugins! " + Main.plugin.getName() + "!");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/members/wisehollow.14804/"));
                player.spigot().sendMessage(message);
            } else
                sender.sendMessage(ChatColor.BLUE + Main.plugin.getName() + " was created by WiseHollow. Check out my other plugins on my SpigotMC profile!");
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("version")) {
                sender.sendMessage(ChatColor.BLUE + Main.plugin.getName() + "'s current version: " + Main.plugin.getDescription().getVersion());
                if (UpdateManager.isUpdateAvailable())
                    sender.sendMessage(Language.PREFIX + "Update is available.");
                else
                    sender.sendMessage(Language.PREFIX + "Everything is up-to-date.");
                return true;
            } else if (args[0].equalsIgnoreCase("update")) {
                if (UpdateManager.isUpdateAvailable()) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        TextComponent message = new TextComponent(Language.PREFIX + ChatColor.UNDERLINE + "Click here " +
                                ChatColor.RESET + "" + ChatColor.BLUE + "to get the latest version of " + Main.plugin.getName() + "!");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://goo.gl/yiWQnT"));
                        player.spigot().sendMessage(message);
                    }
                    else
                        sender.sendMessage("Go to https://goo.gl/yiWQnT to get the latest version of " + Main.plugin.getName() + "!");
                }
                else
                    sender.sendMessage(Language.PREFIX + "Everything is up-to-date.");
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                Main.plugin.reloadConfig();
                Settings.load();
                sender.sendMessage(Language.PREFIX + "Configuration has been reloaded!");
                return true;
            } else if (args[0].equalsIgnoreCase("regenConfig")) {
                File file = new File("plugins" + File.separator + Main.plugin.getName() + File.separator + "config.yml");
                file.delete();
                Main.plugin.saveDefaultConfig();
                Main.plugin.reloadConfig();
                Settings.load();
                sender.sendMessage(Language.PREFIX + "Configuration has been regenerated and reloaded!");
                return true;
            }
        }

        return false;
    }
}
