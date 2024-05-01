package com.github.gavvydizzle.vouchers.rarity;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RarityManager {

    private final Vouchers instance;
    private final Map<String, Rarity> rarityMap;

    public RarityManager(Vouchers instance) {
        this.instance = instance;
        rarityMap = new HashMap<>();

        reload();
    }

    public void reload() {
        FileConfiguration config = instance.getConfig();
        config.addDefault("rarity", Map.of());

        rarityMap.clear();

        ConfigurationSection section = config.getConfigurationSection("rarity");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection raritySection = section.getConfigurationSection(key);
            assert raritySection != null;

            raritySection.addDefault("id", "todo");
            raritySection.addDefault("colorCode", "&7");
            raritySection.addDefault("name", "todo");

            String id = raritySection.getString("id");
            assert id != null;
            id = id.toLowerCase();
            if (id.equals("todo")) {
                Bukkit.getLogger().warning("Invalid rarity id 'todo'. This entry will be ignored");
                continue;
            }
            else if (rarityMap.containsKey(id)) {
                Bukkit.getLogger().warning("You have already defined a rarity with the id '" + id + "' in config.yml. This entry will be ignored");
                continue;
            }

            String colorCode = raritySection.getString("colorCode");
            String name = Colors.conv(colorCode + raritySection.getString("name"));

            rarityMap.put(id, new Rarity(id, colorCode, name));
        }
    }

    @Nullable
    public Rarity getRarity(@Nullable String id) {
        if (id == null) return null;
        return rarityMap.get(id.toLowerCase());
    }
}