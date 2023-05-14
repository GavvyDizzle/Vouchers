package com.github.gavvydizzle.vouchers.commands;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.gavvydizzle.vouchers.commands.admin.*;
import com.github.gavvydizzle.vouchers.configs.CommandsConfig;
import com.github.gavvydizzle.vouchers.gui.GUIManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminCommandManager implements TabExecutor {

    private final PluginCommand command;
    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandStrings = new ArrayList<>();
    private final String permissionPrefix;
    private String commandDisplayName, helpCommandPadding;

    public AdminCommandManager(PluginCommand command, VoucherManager voucherManager, GUIManager guiManager) {
        this.command = command;
        command.setExecutor(this);
        permissionPrefix =  command.getPermission() + ".";

        loadCommandName();

        if (Vouchers.getInstance().isRewardsInventoryLoaded()) {
            subcommands.add(new AddToRewardsMenuCommand(this, voucherManager));
        }
        subcommands.add(new AdminGiveVoucherCommand(this, voucherManager));
        subcommands.add(new AdminHelpCommand(this));
        subcommands.add(new AdminListCommand(this, guiManager));
        subcommands.add(new AdminReloadCommand(this, voucherManager));
        Collections.sort(subcommands);

        for (SubCommand subCommand : subcommands) {
            subcommandStrings.add(subCommand.getName());
        }

        reload();
    }

    private void loadCommandName() {
        FileConfiguration config = CommandsConfig.get();
        config.addDefault("commandDisplayName.admin", command.getName());
        commandDisplayName = config.getString("commandDisplayName.admin");
    }

    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.options().copyDefaults(true);
        config.addDefault("commandDisplayName.admin", command.getName());
        config.addDefault("helpCommandPadding.admin", "&6-----(({page}/{max_page}) " + Vouchers.getInstance().getName() + " Admin Commands)-----");

        for (SubCommand subCommand : subcommands) {
            CommandsConfig.setAdminDescriptionDefault(subCommand);
        }
        CommandsConfig.save();

        commandDisplayName = config.getString("commandDisplayName.admin");
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.admin"));
    }

    public String getCommandDisplayName() {
        return commandDisplayName;
    }

    public String getHelpCommandPadding() {
        return helpCommandPadding;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < getSubcommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {

                    SubCommand subCommand = subcommands.get(i);

                    if (!subCommand.hasPermission(sender)) {
                        sender.sendMessage(ChatColor.RED + "Insufficient permission");
                        return true;
                    }

                    subCommand.perform(sender, args);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Invalid command");
        }
        sender.sendMessage(ChatColor.YELLOW + "Use '/" + commandDisplayName + " help' to see a list of valid commands");

        return true;
    }

    public ArrayList<SubCommand> getSubcommands(){
        return subcommands;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> subcommandsArguments = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], subcommandStrings, subcommandsArguments);

            return subcommandsArguments;
        }
        else if (args.length >= 2) {
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    return subcommand.getSubcommandArguments(sender, args);
                }
            }
        }

        return null;
    }

    public String getPermissionPrefix() {
        return permissionPrefix;
    }
}