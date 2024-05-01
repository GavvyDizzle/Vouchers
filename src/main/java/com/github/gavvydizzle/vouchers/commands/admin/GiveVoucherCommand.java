package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherItem;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GiveVoucherCommand extends SubCommand {

    private final VoucherManager voucherManager;

    public GiveVoucherCommand(AdminCommandManager adminCommandManager, VoucherManager voucherManager) {
        this.voucherManager = voucherManager;

        setName("give");
        setDescription("Adds the voucher to the player's inventory");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " give <player> <id> [amount]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player");
            return;
        }

        VoucherItem voucherItem = voucherManager.getVoucherItem(args[2]);
        if (voucherItem == null) {
            sender.sendMessage(ChatColor.RED + "No voucher exists with the id: " + args[2]);
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "'" + args[3] + " is not a valid amount");
                return;
            }

            if (!Numbers.isWithinRange(amount, 1, 64)) {
                sender.sendMessage(ChatColor.RED + "The amount must be between 1 and 64. You requested " + amount);
                return;
            }

            ItemStack itemStack = voucherItem.getItem();
            itemStack.setAmount(amount);
            player.getInventory().addItem(itemStack);
        }
        else {
            player.getInventory().addItem(voucherItem.getItem());
        }
        sender.sendMessage(ChatColor.GREEN + "Successfully gave " + player.getName() + " " + amount + " " + voucherItem.getId());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return null;
        }
        else if (args.length == 3) {
            ArrayList<String> list = new ArrayList<>();
            StringUtil.copyPartialMatches(args[2], voucherManager.getVoucherIDs(), list);
            return list;
        }
        return Collections.emptyList();
    }

}