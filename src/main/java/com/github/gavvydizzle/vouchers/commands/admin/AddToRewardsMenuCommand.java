package com.github.gavvydizzle.vouchers.commands.admin;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherItem;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.SubCommand;
import me.gavvydizzle.rewardsinventory.api.RewardsInventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

// Requires RewardsInventory active to be loaded!
public class AddToRewardsMenuCommand extends SubCommand {

    private final RewardsInventoryAPI rewardsInventoryAPI;
    private final VoucherManager voucherManager;

    public AddToRewardsMenuCommand(AdminCommandManager adminCommandManager, VoucherManager voucherManager) {
        this.voucherManager = voucherManager;
        rewardsInventoryAPI = RewardsInventoryAPI.getInstance();

        setName("reward");
        setDescription("Adds a voucher to the player's /rew pages inventory");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " reward <player> <id> <menuID> [amount]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getPlayer(args[1]);
        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid player.");
                return;
            }
        }

        VoucherItem voucherItem = voucherManager.getVoucherItem(args[2]);
        if (voucherItem == null) {
            sender.sendMessage(ChatColor.RED + "No voucher exists with the id: " + args[2]);
            return;
        }

        int pageMenuID = rewardsInventoryAPI.getMenuID(args[3]);
        if (pageMenuID == -1) {
            sender.sendMessage(ChatColor.RED + "No menu exists with the id: " + args[3]);
            return;
        }

        int amount;
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
            }
            catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "'" + args[4] + " is not a valid amount");
                return;
            }

            ItemStack itemStack = voucherItem.getItem();
            if (!Numbers.isWithinRange(amount, 1, itemStack.getMaxStackSize())) {
                sender.sendMessage(ChatColor.RED + "The amount must be between 1 and 64. You requested " + amount);
                return;
            }

            itemStack.setAmount(amount);
            if (!rewardsInventoryAPI.addItem(offlinePlayer, pageMenuID, itemStack)) {
                sender.sendMessage(ChatColor.RED + "Failed to add the item");
                return;
            }
        }
        else {
            amount = 1;
            if (!rewardsInventoryAPI.addItem(offlinePlayer, pageMenuID, voucherItem.getItem())) {
                sender.sendMessage(ChatColor.RED + "Failed to add the item");
                return;
            }
        }
        if (sender instanceof Player) sender.sendMessage(ChatColor.GREEN + "Successfully put " + amount + " " + voucherItem.getId() + " into " + offlinePlayer.getName() + "'s /rew " + args[3] + " menu");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            return null;
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], voucherManager.getVoucherIDs(), list);
        }
        else if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], rewardsInventoryAPI.getPageMenuNames(), list);
        }

        return list;
    }
}