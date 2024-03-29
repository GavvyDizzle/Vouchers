package com.github.gavvydizzle.vouchers.vouchers;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class VoucherManager implements Listener  {

    private final Vouchers instance;
    private final File vouchersFolder;
    private final NamespacedKey voucherKey;
    private final HashMap<UUID, String> confirmations;
    private final Map<String, VoucherItem> vouchers;
    private final ArrayList<VoucherItem> voucherList;

    private String confirmationMessage, ownedMessage;
    private int confirmationDelayTicks;
    private boolean checkForPermission, sortVouchersAlphabetically;
    private List<String> claimCommands;

    public VoucherManager(Vouchers instance) {
        this.instance = instance;
        vouchersFolder = new File(instance.getDataFolder(), "vouchers");
        voucherKey = new NamespacedKey(instance, "voucher_id");

        confirmations = new HashMap<>();
        vouchers = new HashMap<>();
        voucherList = new ArrayList<>();
        reload();
    }

    public void reload() {
        FileConfiguration config = instance.getConfig();
        config.options().copyDefaults(true);
        config.addDefault("confirmationDelaySeconds", 5);
        config.addDefault("messages.hasPermission", "&cYou already have access to this cosmetic!");
        config.addDefault("messages.confirmation", "&6&l(!) &6Click again to claim {voucher}");
        config.addDefault("checkForPermission", true);
        config.addDefault("sortVouchersAlphabetically", false);
        config.addDefault("onClaimCommands", Collections.singletonList("lp user {player} permission set {permission}"));
        instance.saveConfig();

        confirmationDelayTicks = config.getInt("confirmationDelaySeconds") * 20;
        confirmationMessage = Colors.conv(config.getString("messages.confirmation")).trim();
        ownedMessage = Colors.conv(config.getString("messages.hasPermission")).trim();
        checkForPermission = config.getBoolean("checkForPermission");
        sortVouchersAlphabetically = config.getBoolean("sortVouchersAlphabetically");
        claimCommands = config.getStringList("onClaimCommands");

        reloadAllItems();
    }

    /**
     * Reloads all custom items from all .yml files in the plugin directory
     */
    private void reloadAllItems() {
        vouchers.clear();
        voucherList.clear();

        try {
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                vouchersFolder.mkdir();
                parseFolderForFiles(vouchersFolder);

                voucherList.clear();
                voucherList.addAll(vouchers.values());

                if (sortVouchersAlphabetically) {
                    Collections.sort(voucherList);
                }

                instance.getGuiManager().reloadAllGUIs();
            });
        }
        catch (Exception e) {
            instance.getLogger().severe("Failed to load Vouchers");
            instance.getLogger().severe(e.getMessage());
        }
    }

    private void parseFolderForFiles(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                parseFolderForFiles(fileEntry);
            } else {
                if (!fileEntry.getName().endsWith(".yml")) continue;

                final FileConfiguration config = YamlConfiguration.loadConfiguration(fileEntry);

                if (config.getConfigurationSection("vouchers") == null) {
                    Bukkit.getLogger().warning("The file " + fileEntry.getName() + " is empty. Make sure it has the 'vouchers:' section");
                }
                else {
                    for (String key : Objects.requireNonNull(config.getConfigurationSection("vouchers")).getKeys(false)) {
                        String path = "vouchers." + key;

                        String id = key.toLowerCase();
                        if (vouchers.containsKey(id)) {
                            Bukkit.getLogger().warning("You have defined '" + key + "' multiple times. This occurrence is in " + fileEntry.getName());
                            continue;
                        }

                        try {
                            vouchers.put(key.toLowerCase(), new VoucherItem(
                                    key.toLowerCase(),
                                    config.getString(path + ".permission"),
                                    config.getString(path + ".displayName"),
                                    config.getString(path + ".material"),
                                    config.getStringList(path + ".lore"),
                                    config.getStringList(path + ".extraCommands"),
                                    config.getString(path + ".onClaimMessage"),
                                    config.getBoolean(path + ".glow"),
                                    config.getInt(path + ".customModelData")
                            ));
                        }
                        catch (Exception e) {
                            instance.getLogger().warning("Failed to add the voucher " + key + " from file " + fileEntry.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @EventHandler()
    public void onClick(PlayerInteractEvent e) {
        // Allow if:
        // 1. The player right-clicked a block and was not denied from using their item
        //    OR The player right-clicked the air
        // 2. The targeted hand is the main hand (to remove double clicks of main hand and offhand)
        if (( (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.useItemInHand() != Event.Result.DENY) || e.getAction() == Action.RIGHT_CLICK_AIR) && e.getHand() == EquipmentSlot.HAND) {
            Player player = e.getPlayer();
            ItemStack voucher = player.getInventory().getItemInMainHand();

            if (!isVoucher(voucher)) return;
            e.setCancelled(true);

            VoucherItem voucherItem = getVoucherItem(voucher);
            if (voucherItem == null) return;

            // Allow admins to bypass the permission check to see if everything works correctly
            boolean isAdmin = player.hasPermission("vouchers.admin");

            if (checkForPermission && voucherItem.hasPermission(player) && !isAdmin) {
                if (!ownedMessage.isEmpty()) {
                    player.sendMessage(ownedMessage);
                }
                return;
            }

            if (!confirmations.containsKey(player.getUniqueId())) {
                if (!confirmationMessage.isEmpty()) {
                    player.sendMessage(confirmationMessage.replace("{voucher}", voucherItem.getItemName()));
                }
                confirmations.put(player.getUniqueId(), voucherItem.getId());

                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () ->
                        confirmations.remove(player.getUniqueId()), confirmationDelayTicks);

                return;
            }

            if (!confirmations.get(player.getUniqueId()).equals(voucherItem.getId())) {
                if (!confirmationMessage.isEmpty()) {
                    player.sendMessage(confirmationMessage.replace("{voucher}", voucherItem.getItemName()));
                }
                confirmations.put(player.getUniqueId(), voucherItem.getId());
                return;
            }

            confirmations.remove(player.getUniqueId());
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

            // Claim commands specified in config.yml
            if (!voucherItem.getPermission().isEmpty()) {
                for (String cmd : claimCommands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName())
                            .replace("{permission}", voucherItem.getPermission()));
                }
            }

            // Run other commands and obtain message
            voucherItem.claim(player);
        }
    }

    public boolean isVoucher(ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(voucherKey, PersistentDataType.STRING);
    }

    /**
     * Gets the VoucherItem associated with this ItemStack
     * @param itemStack The ItemStack
     * @return The VoucherItem or null if none exists for this item
     */
    public VoucherItem getVoucherItem(ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) return null;
        return vouchers.get(itemStack.getItemMeta().getPersistentDataContainer().get(voucherKey, PersistentDataType.STRING));
    }

    /**
     * @param id The id
     * @return The VoucherItem or null if none exists for this id
     */
    public VoucherItem getVoucherItem(@NotNull String id) {
        return vouchers.get(id);
    }

    public ArrayList<VoucherItem> getVouchers() {
        return voucherList;
    }

    public Set<String> getVoucherIDs() {
        return vouchers.keySet();
    }

    protected NamespacedKey getVoucherKey() {
        return voucherKey;
    }

}
