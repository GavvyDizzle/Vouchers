package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.gui.GUIManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminListCommand extends SubCommand {

    private final GUIManager guiManager;

    public AdminListCommand(AdminCommandManager adminCommandManager, GUIManager guiManager) {
        this.guiManager = guiManager;

        setName("list");
        setDescription("Opens the vouchers list menu");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " list");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            guiManager.openVoucherListInventory((Player) sender);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}