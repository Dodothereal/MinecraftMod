package com.example.examplemod.config;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MinerConfig {

    private Configuration config;
    private boolean humanizeEnabled = true;
    private int range = 5;
    private final Map<String, Boolean> oreEnabled = new HashMap<>();

    public MinerConfig() {
        // Initialize ore enabled map with defaults
        oreEnabled.put("Diamond", true);
        oreEnabled.put("Emerald", true);
        oreEnabled.put("Gold", true);
        oreEnabled.put("Iron", true);
        oreEnabled.put("Coal", false);
        oreEnabled.put("Redstone", false);
        oreEnabled.put("Lapis", false);

        // Load config
        File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/oreminer.cfg");
        config = new Configuration(configFile);
        loadConfig();
    }

    private void loadConfig() {
        config.load();

        // Load settings
        humanizeEnabled = config.getBoolean("humanizeEnabled", "settings", humanizeEnabled,
                "Enable humanized rotations");

        range = config.getInt("range", "settings", range, 3, 6,
                "Ore detection range");

        // Load ore enabled settings
        for (String oreName : oreEnabled.keySet()) {
            boolean defaultValue = oreEnabled.get(oreName);
            boolean value = config.getBoolean(oreName, "ores", defaultValue,
                    "Enable mining of " + oreName + " ore");
            oreEnabled.put(oreName, value);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public void saveConfig() {
        // Save settings
        config.get("settings", "humanizeEnabled", humanizeEnabled).set(humanizeEnabled);
        config.get("settings", "range", range).set(range);

        // Save ore enabled settings
        for (Map.Entry<String, Boolean> entry : oreEnabled.entrySet()) {
            config.get("ores", entry.getKey(), entry.getValue()).set(entry.getValue());
        }

        config.save();
    }

    public boolean isHumanizeEnabled() {
        return humanizeEnabled;
    }

    public void setHumanizeEnabled(boolean humanizeEnabled) {
        this.humanizeEnabled = humanizeEnabled;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public boolean isOreEnabled(String oreName) {
        return oreEnabled.getOrDefault(oreName, false);
    }

    public void setOreEnabled(String oreName, boolean enabled) {
        oreEnabled.put(oreName, enabled);
    }
}