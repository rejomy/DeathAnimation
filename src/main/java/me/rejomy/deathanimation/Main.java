package me.rejomy.deathanimation;

import me.rejomy.deathanimation.config.Config;
import me.rejomy.deathanimation.listener.PlayerDeathListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final Config config = new Config();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config.load(getConfig());
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(config, this), this);
    }
}
