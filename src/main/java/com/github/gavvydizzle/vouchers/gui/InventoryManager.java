package com.github.gavvydizzle.vouchers.gui;

import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import com.github.mittenmc.serverutils.gui.ClickableMenu;
import com.github.mittenmc.serverutils.gui.MenuManager;
import com.github.mittenmc.serverutils.gui.filesystem.FileSystemMenu;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryManager extends MenuManager {

    private final VoucherManager voucherManager;

    public InventoryManager(JavaPlugin instance, VoucherManager voucherManager) {
        super(instance);
        this.voucherManager = voucherManager;
    }

    public void openFileSystemMenu(Player player) {
        FileSystemMenu menu = new FileSystemMenu("Vouchers Database", voucherManager.getRootNode());
        openMenu(player, menu);
    }

    public void refreshFileSystemMenus() {
        for (ClickableMenu menu : getViewers().values()) {
            if (menu instanceof FileSystemMenu fileSystemMenu) {
                fileSystemMenu.refresh(voucherManager.getRootNode());
            }
        }
    }
}