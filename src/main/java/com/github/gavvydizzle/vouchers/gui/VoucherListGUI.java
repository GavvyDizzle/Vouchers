package com.github.gavvydizzle.vouchers.gui;

import com.github.gavvydizzle.vouchers.vouchers.VoucherItem;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.ColoredItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoucherListGUI {

    private final VoucherManager voucherManager;
    private final int inventorySize;
    private int maxPage;
    private final int pageDownSlot;
    private final int pageInfoSlot;
    private final int pageUpSlot;
    private final ItemStack pageInfoItem;
    private final ItemStack previousPageItem;
    private final ItemStack nextPageItem;

    private final String inventoryName = ChatColor.GOLD + "Item List";
    private final ItemStack pageRowFiller;
    private final Map<UUID, Integer> playersInGUI;

    public VoucherListGUI(VoucherManager voucherManager) {
        this.voucherManager = voucherManager;

        inventorySize = 54;
        pageDownSlot = 48;
        pageInfoSlot = 49;
        pageUpSlot = 50;
        pageRowFiller = ColoredItems.LIGHT_GRAY.getGlass();

        pageInfoItem = new ItemStack(Material.PAPER);

        previousPageItem = new ItemStack(Material.PAPER);
        ItemMeta prevPageMeta = previousPageItem.getItemMeta();
        assert prevPageMeta != null;
        prevPageMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
        previousPageItem.setItemMeta(prevPageMeta);

        nextPageItem = new ItemStack(Material.PAPER);
        ItemMeta nextPageMeta = nextPageItem.getItemMeta();
        assert nextPageMeta != null;
        nextPageMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
        nextPageItem.setItemMeta(nextPageMeta);

        playersInGUI = new HashMap<>();
    }

    public void reload() {
        maxPage = voucherManager.getVouchers().size() / 45 + 1;
    }

    /**
     * Opens this inventory for the player.
     * @param player The player to open the inventory for.
     */
    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryName);
        ArrayList<VoucherItem> voucherItems = voucherManager.getVouchers();

        for (int i = 0; i < 45; i++) {
            try {
                inventory.setItem(i, voucherItems.get(i).getItemListItem());
            }
            catch (Exception e) {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, pageRowFiller);
        }
        inventory.setItem(pageDownSlot, previousPageItem);
        inventory.setItem(pageInfoSlot, getPageItem(1));
        inventory.setItem(pageUpSlot, nextPageItem);

        player.openInventory(inventory);
        playersInGUI.put(player.getUniqueId(), 1);
    }

    /**
     * Handles a click to this inventory.
     * @param player The player who clicked.
     * @param slot The slot clicked.
     */
    public void handleClick(Player player, int slot) {
        int page = playersInGUI.get(player.getUniqueId());
        if (slot == pageUpSlot) {
            if (page < maxPage) {
                playersInGUI.put(player.getUniqueId(), page + 1);
                updatePageItems(player);
                player.getOpenInventory().getTopInventory().setItem(pageInfoSlot, getPageItem(page + 1));
            }
        }
        else if (slot == pageDownSlot) {
            if (page > 1) {
                playersInGUI.put(player.getUniqueId(), page - 1);
                updatePageItems(player);
                player.getOpenInventory().getTopInventory().setItem(pageInfoSlot, getPageItem(page - 1));
            }
        }
        else if (slot < 45) {
            int index = getItemIndexSlot(page, slot);
            ArrayList<VoucherItem> voucherItems = voucherManager.getVouchers();
            if (voucherItems.size() > index) {
                player.getInventory().addItem(voucherItems.get(index).getItem());
            }
        }
    }

    //Update slots 0-45 when the page is turned
    private void updatePageItems(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        int page = playersInGUI.get(player.getUniqueId());

        ArrayList<VoucherItem> voucherItems = voucherManager.getVouchers();

        for (int i = getItemIndexSlot(page, 0); i < getItemIndexSlot(page, 45); i++) {
            try {
                inventory.setItem(i % 45, voucherItems.get(i).getItemListItem());
            }
            catch (Exception e) {
                inventory.setItem(i % 45, new ItemStack(Material.AIR));
            }
        }
    }

    protected ItemStack getPageItem(int page) {
        ItemStack pageInfo = pageInfoItem.clone();
        ItemMeta meta = pageInfo.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.YELLOW + "Page " + page + "/" + maxPage);
        pageInfo.setItemMeta(meta);
        return pageInfo;
    }

    private int getItemIndexSlot(int page, int slot) {
        return (page - 1) * 45 + slot;
    }

}