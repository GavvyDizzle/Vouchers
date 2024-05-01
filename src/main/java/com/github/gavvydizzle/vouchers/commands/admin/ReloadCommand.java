package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ReloadCommand extends SubCommand {

    private final Vouchers instance;

    public ReloadCommand(AdminCommandManager adminCommandManager, Vouchers instance) {
        this.instance = instance;

        setName("reload");
        setDescription("Reload this plugin");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " reload");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads all data from the config";
    }

    @Override
    public String getSyntax() {
        return "/voucher reload";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        try {
            instance.reloadConfig();
            instance.getRarityManager().reload();
            instance.getVoucherManager().reload();
            instance.saveConfig();
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "[Vouchers] Encountered an error when reloading. Check the console");
            instance.getLogger().log(Level.SEVERE, "Plugin reload failed", e);
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "[Vouchers] Successfully reloaded");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}