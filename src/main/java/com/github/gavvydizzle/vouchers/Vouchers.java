package com.github.gavvydizzle.vouchers;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.gui.InventoryManager;
import com.github.gavvydizzle.vouchers.rarity.RarityManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Vouchers extends JavaPlugin {

    @Getter private static Vouchers instance;
    private RarityManager rarityManager;
    private VoucherManager voucherManager;
    private InventoryManager inventoryManager;
    private boolean isRewardsInventoryLoaded;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);

        instance = this;
        isRewardsInventoryLoaded = getServer().getPluginManager().getPlugin("RewardsInventory") != null;

        rarityManager = new RarityManager(this);
        voucherManager = new VoucherManager(this, rarityManager);
        inventoryManager = new InventoryManager(this, voucherManager);

        new AdminCommandManager(getCommand("voucher"), this, voucherManager, inventoryManager);

        saveConfig();
    }
}