package com.github.gavvydizzle.vouchers.vouchers;

import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.gui.pages.ItemGenerator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoucherItem implements Comparable<VoucherItem>, ItemGenerator {

    @Getter
    private final String id, itemName, permission;
    private final String onClaimMessage;
    private final List<String> extraCommands;
    private final ItemStack item, itemListItem;

    public VoucherItem(@NotNull String id, @NotNull String permission, @NotNull String onClaimMessage, @NotNull List<String> extraCommands, @NotNull ItemStack itemStack) {
        this.id = id;
        this.permission = permission;
        this.onClaimMessage = onClaimMessage;
        this.extraCommands = extraCommands;

        this.item = itemStack;
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        this.itemName = meta.getDisplayName();
        int customModelID = meta.hasCustomModelData() ? meta.getCustomModelData() : -1;

        itemListItem = item.clone();
        meta = itemListItem.getItemMeta();
        assert meta != null;
        List<String> itemListLore = meta.getLore();
        assert itemListLore != null;
        itemListLore.add(Colors.conv("&8----------------------"));
        itemListLore.add(Colors.conv("&6id: &e" + id));
        itemListLore.add(Colors.conv("&6Permission: &e" + permission));
        if (customModelID > 0) itemListLore.add(Colors.conv("&6custom_model_data: &e" + customModelID));
        meta.setLore(itemListLore);
        itemListItem.setItemMeta(meta);
    }

    /**
     * Runs the voucher-specific commands and sends the claim message
     * @param player The player
     */
    public void claim(Player player) {
        for (String cmd : extraCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
        }

        if (!onClaimMessage.isEmpty()) {
            player.sendMessage(onClaimMessage);
        }
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    @Override
    public @NotNull ItemStack getMenuItem(Player player) {
        return itemListItem;
    }

    @Override
    public @Nullable ItemStack getPlayerItem(Player player) {
        return getItem();
    }

    public ItemStack getItem() {
        return item.clone();
    }

    @Override
    public int compareTo(@NotNull VoucherItem o) {
        return id.compareTo(o.id);
    }
}
