package me.rejomy.deathanimation.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Config {

    private final List<World> enabledWorlds = new ArrayList<>();
    private boolean copyEntityEquipment;
    private int maxDistanceToEntity;
    private int removeEntityTicks;

    public void load(FileConfiguration config) {
        config.getStringList("enabled-worlds").forEach(worldName -> {
            World world = Bukkit.getWorld(worldName);

            // If world does not load
            if (world == null) {
                throw new IllegalArgumentException("World " + worldName + " not found");
            }

            enabledWorlds.add(world);
        });

        maxDistanceToEntity = config.getInt("max-distance-to-entity", 50);
        removeEntityTicks = config.getInt("remove-entity-ticks", 10);
        copyEntityEquipment = config.getBoolean("copy-entity-equipment", true);
    }
}
