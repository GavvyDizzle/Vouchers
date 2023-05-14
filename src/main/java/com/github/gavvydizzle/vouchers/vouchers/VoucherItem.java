package com.github.gavvydizzle.vouchers.vouchers;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoucherItem implements Comparable<VoucherItem> {

    private final String id, permission, onClaimMessage;
    private final ItemStack item, itemListItem;
    private final List<String> extraCommands;

    public VoucherItem(String id,
                       String permission,
                       String displayName,
                       String material,
                       List<String> lore,
                       List<String> extraCommands,
                       String onClaimMessage,
                       boolean isEnchanted,
                       int customModelID) {

        this.id = id;
        this.permission = permission;
        this.extraCommands = extraCommands;
        this.onClaimMessage = Colors.conv(onClaimMessage);

        item = new ItemStack(ConfigUtils.getMaterial(material, Material.NAME_TAG));
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (isEnchanted) {
            meta.addEnchant(Enchantment.OXYGEN, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setDisplayName(Colors.conv(displayName));
        meta.setLore(Colors.conv(lore));
        meta.getPersistentDataContainer().set(Vouchers.getInstance().getVoucherManager().getVoucherKey(), PersistentDataType.STRING, id);
        if (customModelID > 0) meta.setCustomModelData(customModelID);
        item.setItemMeta(meta);

        itemListItem = item.clone();
        meta = itemListItem.getItemMeta();
        assert meta != null;
        ArrayList<String> itemListLore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
        itemListLore.add(ChatColor.DARK_GRAY + "----------------------");
        itemListLore.add(ChatColor.YELLOW + "id: " + ChatColor.GREEN + id);
        if (customModelID > 0) itemListLore.add(ChatColor.YELLOW + "custom_model_data: " + ChatColor.GREEN + customModelID);
        meta.setLore(itemListLore);
        itemListItem.setItemMeta(meta);
    }

    /**
     * Runs all commands for this voucher while replacing %player% with the player's name
     * @param player The player
     */
    public void runExtraCommands(OfflinePlayer player) {
        for (String cmd : extraCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", Objects.requireNonNull(player.getName())));
        }
    }

    public void sendOnClaimMessage(Player player) {
        if (!onClaimMessage.trim().isEmpty()) {
            player.sendMessage(onClaimMessage);
        }
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }


    public String getId() {
        return id;
    }

    public String getPermission() {
        return permission;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public ItemStack getItemListItem() {
        return itemListItem;
    }

    @Override
    public int compareTo(@NotNull VoucherItem o) {
        return id.compareTo(o.id);
    }
}
