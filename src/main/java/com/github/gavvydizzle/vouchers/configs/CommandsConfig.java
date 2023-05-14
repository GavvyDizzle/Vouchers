package com.github.gavvydizzle.vouchers.configs;

import com.github.gavvydizzle.vouchers.Vouchers;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CommandsConfig {

    private static File file;
    private static FileConfiguration fileConfiguration;

    static {
        setup();
        save();
    }

    //Finds or generates the config file
    public static void setup() {
        file = new File(Vouchers.getInstance().getDataFolder(), "commands.yml");
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return fileConfiguration;
    }

    public static void save() {
        try {
            fileConfiguration.save(file);
        }
        catch (IOException e) {
            System.out.println("Could not save file");
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }


    public static void setAdminDescriptionDefault(SubCommand subCommand) {
        fileConfiguration.addDefault("descriptions.admin." + subCommand.getName(), subCommand.getDescription());
    }

    /**
     * @param subCommand The SubCommand
     * @return The description of this SubCommand as defined in this config file
     */
    public static String getAdminDescription(SubCommand subCommand) {
        return fileConfiguration.getString("descriptions.admin." + subCommand.getName());
    }

}

