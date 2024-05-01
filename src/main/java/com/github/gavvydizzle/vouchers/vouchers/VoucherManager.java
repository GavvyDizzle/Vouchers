package com.github.gavvydizzle.vouchers.vouchers;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.gavvydizzle.vouchers.rarity.Rarity;
import com.github.gavvydizzle.vouchers.rarity.RarityManager;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import com.github.mittenmc.serverutils.ItemStackUtils;
import com.github.mittenmc.serverutils.gui.filesystem.tree.ItemFileNode;
import com.github.mittenmc.serverutils.gui.filesystem.tree.ItemNode;
import com.github.mittenmc.serverutils.gui.filesystem.tree.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class VoucherManager implements Listener {

    private final Vouchers instance;
    private final RarityManager rarityManager;
    private final File vouchersFolder;
    private final NamespacedKey voucherKey;
    private final Map<String, VoucherItem> vouchersMap;
    private final Map<UUID, String> confirmationsMap;
    private Node root;

    private Material defaultMaterial;
    @Nullable private Rarity defaultRarity;
    private boolean checkForPermission;
    private int confirmationDelayTicks;
    private List<String> claimCommands;
    private List<String> lorePrefix, loreSuffix;
    private String confirmationMessage, ownedMessage;

    public VoucherManager(Vouchers instance, RarityManager rarityManager) {
        this.instance = instance;
        this.rarityManager = rarityManager;
        instance.getServer().getPluginManager().registerEvents(this, instance);

        vouchersFolder = new File(instance.getDataFolder(), "vouchers");
        voucherKey = new NamespacedKey(instance, "voucher_id");

        vouchersMap = new HashMap<>();
        confirmationsMap = new HashMap<>();

        reload();
    }

    public void reload() {
        FileConfiguration config = instance.getConfig();
        config.addDefault("vouchers.default_material", Material.NAME_TAG.name());
        config.addDefault("vouchers.default_rarity", "");
        config.addDefault("vouchers.checkForPermission", true);
        config.addDefault("vouchers.confirmationDelaySeconds", 5);
        config.addDefault("vouchers.onClaimCommands", Collections.singletonList("lp user {player} permission set {permission}"));
        config.addDefault("vouchers.lore.prefix", List.of());
        config.addDefault("vouchers.lore.suffix", List.of());
        config.addDefault("messages.confirmation", "&6&l(!) &6Click again to claim {voucher_item_name}");
        config.addDefault("messages.hasPermission", "&cYou already have access to this claimable!");

        defaultMaterial = ConfigUtils.getMaterial(config.getString("vouchers.material"), Material.NAME_TAG);
        defaultRarity = rarityManager.getRarity(config.getString("vouchers.default_rarity"));
        checkForPermission = config.getBoolean("vouchers.checkForPermission");
        confirmationDelayTicks = config.getInt("vouchers.confirmationDelaySeconds") * 20;
        claimCommands = config.getStringList("vouchers.onClaimCommands");
        lorePrefix = Colors.conv(config.getStringList("vouchers.lore.prefix"));
        loreSuffix = Colors.conv(config.getStringList("vouchers.lore.suffix"));
        confirmationMessage = Colors.conv(config.getString("messages.confirmation")).trim();
        ownedMessage = Colors.conv(config.getString("messages.hasPermission")).trim();

        vouchersMap.clear();
        root = new ItemFileNode(null, "root", true);
        parseRecursively(vouchersFolder);
    }

    /**
     * Recursively parses all .yml in this folder
     * @param folder The root folder
     */
    private void parseRecursively(File folder) {
        if (folder.isFile()) {
            throw new RuntimeException("Provided file is not a folder");
        }

        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();

        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            parseFiles(vouchersFolder, root);
            instance.getInventoryManager().refreshFileSystemMenus();
        });
    }

    private void parseFiles(File folder, Node curr) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            String fileName = fileEntry.getName();

            if (fileEntry.isDirectory()) {
                ItemFileNode itemFileNode = new ItemFileNode(curr, fileName, true);
                curr.add(itemFileNode);
                parseFiles(fileEntry, itemFileNode);
            }
            else if (fileName.endsWith(".yml")) {
                //reformatFile(fileEntry);
                readFile(fileEntry, curr);
            }
        }
    }

    private void readFile(File file, Node curr) {
        String fileName = file.getName();

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.options().copyDefaults(true);
        config.addDefault("overrides.material", "");
        config.addDefault("overrides.rarity", "");
        config.addDefault("overrides.lore.prefix", List.of());
        config.addDefault("overrides.lore.suffix", List.of());
        config.addDefault("vouchers", Map.of());

        Material materialOverride = ConfigUtils.getMaterial(config.getString("overrides.material"), Material.AIR);
        @Nullable Rarity rarityOverride = rarityManager.getRarity(config.getString("overrides.rarity"));
        List<String> lorePrefixOverride = Colors.conv(config.getStringList("overrides.lore.prefix"));
        List<String> loreSuffixOverride = Colors.conv(config.getStringList("overrides.lore.suffix"));

        ConfigurationSection vouchersSection = config.getConfigurationSection("vouchers");
        if (vouchersSection == null) return;

        ItemFileNode itemFileNode = new ItemFileNode(curr, fileName, false);
        curr.add(itemFileNode);

        for (String key : vouchersSection.getKeys(false)) {
            ConfigurationSection section = vouchersSection.getConfigurationSection(key);
            if (section == null) continue;

            section.addDefault("permission", "");
            section.addDefault("rarity", "");
            section.addDefault("onClaimMessage", "");
            section.addDefault("extraCommands", List.of());
            section.addDefault("item.name", key);
            section.addDefault("item.lore", List.of());

            String id = key.toLowerCase();
            if (vouchersMap.containsKey(id)) {
                Bukkit.getLogger().warning("You have already defined a voucher with the id '" + id + "' (" + fileName + "). This entry will be ignored");
                continue;
            }

            String permission = section.getString("permission");
            if (permission == null || permission.isBlank()) {
                Bukkit.getLogger().warning("Voucher '" + id + "' in " + fileName + " has no permission defined. This entry will be ignored");
                continue;
            }

            String onClaimMessage = Colors.conv(section.getString("onClaimMessage")).trim();
            List<String> extraCommands = section.getStringList("extraCommands");

            ItemStack itemStack = ConfigUtils.getItemStack(section.getConfigurationSection("item"), fileName, instance.getLogger());
            if (!section.contains("item.material")) {
                if (materialOverride != Material.AIR) {
                    itemStack.setType(materialOverride);
                } else {
                    itemStack.setType(defaultMaterial);
                }
            }

            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;
            List<String> lore = meta.getLore();
            assert lore != null;
            lore.addAll(0, !lorePrefixOverride.isEmpty() ? lorePrefixOverride : lorePrefix);
            lore.addAll(!loreSuffixOverride.isEmpty() ? loreSuffixOverride : loreSuffix);
            meta.getPersistentDataContainer().set(voucherKey, PersistentDataType.STRING, id);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            Rarity rarity = rarityManager.getRarity(section.getString("rarity"));
            if (rarity == null) {
                if (rarityOverride != null) {
                    rarity = rarityOverride;
                } else {
                    rarity = defaultRarity;
                }
            }
            if (rarity != null) {
                ItemStackUtils.replacePlaceholders(itemStack, Map.of(
                        "{rarity}", rarity.name(),
                        "{rarity_color}", rarity.colorCode())
                );
            }

            VoucherItem voucherItem = new VoucherItem(id, permission, onClaimMessage, extraCommands, itemStack);

            vouchersMap.put(id, voucherItem);
            itemFileNode.add(new ItemNode<>(curr, id, voucherItem));

            try {
                config.save(file);
            } catch (Exception e) {
                instance.getLogger().log(Level.SEVERE, "Failed to save " + fileName, e);
            }
        }
    }

    @EventHandler()
    public void onClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND ||
                (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.useItemInHand() == Event.Result.DENY) ||
                 e.getAction() != Action.RIGHT_CLICK_AIR) return;

        // Allow voucher claim (1 and 2):
        // 1. The targeted hand is the main hand
        // 2. The player right-clicked a block and was not denied from using their item
        //    OR The player right-clicked the air

        Player player = e.getPlayer();
        ItemStack voucher = player.getInventory().getItemInMainHand();

        VoucherItem voucherItem = getVoucherItem(voucher);
        if (voucherItem == null) return;

        e.setCancelled(true);

        // Allow admins to bypass the permission check to see if everything works correctly
        boolean isAdmin = player.hasPermission("vouchers.admin");

        if (!isAdmin && checkForPermission && voucherItem.hasPermission(player)) {
            if (!ownedMessage.isEmpty()) {
                player.sendMessage(ownedMessage);
            }
            return;
        }

        if (!confirmationsMap.containsKey(player.getUniqueId())) {
            if (!confirmationMessage.isEmpty()) {
                player.sendMessage(confirmationMessage.replace("{voucher}", voucherItem.getItemName()));
            }
            confirmationsMap.put(player.getUniqueId(), voucherItem.getId());

            Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () ->
                    confirmationsMap.remove(player.getUniqueId()), confirmationDelayTicks);

            return;
        }

        if (!confirmationsMap.get(player.getUniqueId()).equals(voucherItem.getId())) {
            if (!confirmationMessage.isEmpty()) {
                player.sendMessage(confirmationMessage.replace("{voucher}", voucherItem.getItemName()));
            }
            confirmationsMap.put(player.getUniqueId(), voucherItem.getId());
            return;
        }

        confirmationsMap.remove(player.getUniqueId());
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

        for (String cmd : claimCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd
                    .replace("{player}", player.getName())
                    .replace("{permission}", voucherItem.getPermission())
            );
        }

        // Run other commands and obtain message
        voucherItem.claim(player);
    }

    //***************************//
    // VOUCHER CANCELLED ACTIONS //
    //***************************//

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        if (isVoucherItem(e.getItemInHand())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onEntityInteraction(PlayerInteractEntityEvent e) {
        if (isVoucherItem(e.getPlayer().getInventory().getItem(e.getHand()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onEat(PlayerItemConsumeEvent e) {
        if (isVoucherItem(e.getItem())) {
            e.setCancelled(true);
        }
    }


    /**
     * @param item The item
     * @return If this item has voucher data
     */
    private boolean isVoucherItem(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return false;
        return item.getItemMeta().getPersistentDataContainer().has(voucherKey, PersistentDataType.STRING);
    }

    /**
     * Gets the VoucherItem associated with this ItemStack
     * @param item The item
     * @return The VoucherItem or null if none exists for this item
     */
    private VoucherItem getVoucherItem(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return null;
        return vouchersMap.get(item.getItemMeta().getPersistentDataContainer().get(voucherKey, PersistentDataType.STRING));
    }

    /**
     * @param id The id
     * @return The VoucherItem or null if none exists for this id
     */
    public VoucherItem getVoucherItem(@NotNull String id) {
        return vouchersMap.get(id);
    }

    public Set<String> getVoucherIDs() {
        return vouchersMap.keySet();
    }

    public Node getRootNode() {
        return root;
    }
}
