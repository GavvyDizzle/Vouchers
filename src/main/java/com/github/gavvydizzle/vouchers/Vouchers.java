package com.github.gavvydizzle.vouchers;

import com.github.gavvydizzle.vouchers.commands.AdminCommandManager;
import com.github.gavvydizzle.vouchers.gui.GUIManager;
import com.github.gavvydizzle.vouchers.vouchers.VoucherManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Vouchers extends JavaPlugin {

    private static Vouchers instance;
    private VoucherManager voucherManager;
    private GUIManager guiManager;
    private boolean isRewardsInventoryLoaded;

    @Override
    public void onEnable() {
        instance = this;
        isRewardsInventoryLoaded = getServer().getPluginManager().getPlugin("RewardsInventory") != null;

        voucherManager = new VoucherManager(instance);
        guiManager = new GUIManager(voucherManager);

        getServer().getPluginManager().registerEvents(voucherManager, this);
        getServer().getPluginManager().registerEvents(guiManager, this);

        try {
            new AdminCommandManager(Objects.requireNonNull(getCommand("voucher")), voucherManager, guiManager);
        } catch (NullPointerException e) {
            getLogger().severe("The admin command name was changed in the plugin.yml file. Please make it \"voucher\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public static Vouchers getInstance() {
        return instance;
    }

    public VoucherManager getVoucherManager() {
        return voucherManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public boolean isRewardsInventoryLoaded() {
        return isRewardsInventoryLoaded;
    }
}
