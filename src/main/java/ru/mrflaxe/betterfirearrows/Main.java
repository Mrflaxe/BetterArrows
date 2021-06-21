package ru.mrflaxe.betterfirearrows;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        new Listener(this).register();
    }
}