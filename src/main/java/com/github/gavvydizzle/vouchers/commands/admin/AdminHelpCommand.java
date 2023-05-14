package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.configs.CommandsConfig;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdminHelpCommand extends SubCommand {

    private final AdminCommandManager adminCommandManager;

    public AdminHelpCommand(AdminCommandManager adminCommandManager) {
        this.adminCommandManager = adminCommandManager;

        setName("help");
        setDescription("Opens this help menu");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " help");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        String padding = adminCommandManager.getHelpCommandPadding();

        if (!padding.isEmpty()) sender.sendMessage(padding);
        for (SubCommand subCommand : adminCommandManager.getSubcommands()) {
            sender.sendMessage(ChatColor.GOLD + subCommand.getSyntax() + " - " + ChatColor.YELLOW + CommandsConfig.getAdminDescription(subCommand));
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}