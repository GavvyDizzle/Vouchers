package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.configs.CommandsConfig;
import com.github.gavvydizzle.vouchers.gui.GUIManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdminReloadCommand extends SubCommand {

    private final AdminCommandManager adminCommandManager;
    private final VoucherManager voucherManager;

    public AdminReloadCommand(AdminCommandManager adminCommandManager, VoucherManager voucherManager) {
        this.adminCommandManager = adminCommandManager;
        this.voucherManager = voucherManager;

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
            Vouchers.getInstance().reloadConfig();
            voucherManager.reload();

            CommandsConfig.reload();
            adminCommandManager.reload();
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "[Vouchers] Encountered an error when reloading. Check the console");
            e.printStackTrace();
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "[Vouchers] Successfully reloaded");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}