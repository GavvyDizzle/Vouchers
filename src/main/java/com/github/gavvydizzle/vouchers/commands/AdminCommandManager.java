package com.github.gavvydizzle.vouchers.commands;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.gavvydizzle.vouchers.commands.admin.AddToRewardsMenuCommand;
import com.github.gavvydizzle.vouchers.commands.admin.GiveVoucherCommand;
import com.github.gavvydizzle.vouchers.commands.admin.OpenMenuCommand;
import com.github.gavvydizzle.vouchers.commands.admin.ReloadCommand;
import com.github.gavvydizzle.vouchers.gui.InventoryManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.CommandManager;
import org.bukkit.command.PluginCommand;

public class AdminCommandManager extends CommandManager {

    public AdminCommandManager(PluginCommand command, Vouchers instance, VoucherManager voucherManager, InventoryManager inventoryManager) {
        super(command);

        if (instance.isRewardsInventoryLoaded()) {
            registerCommand(new AddToRewardsMenuCommand(this, voucherManager));
        }
        registerCommand(new GiveVoucherCommand(this, voucherManager));
        registerCommand(new OpenMenuCommand(this, inventoryManager));
        registerCommand(new ReloadCommand(this, instance));
        sortCommands();
    }
}