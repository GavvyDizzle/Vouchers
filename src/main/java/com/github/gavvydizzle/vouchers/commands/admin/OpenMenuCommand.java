package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.gui.InventoryManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class OpenMenuCommand extends SubCommand {

    private final InventoryManager inventoryManager;

    public OpenMenuCommand(AdminCommandManager adminCommandManager, InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;

        setName("list");
        setDescription("Opens the vouchers menu");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " list");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            inventoryManager.openFileSystemMenu(player);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}