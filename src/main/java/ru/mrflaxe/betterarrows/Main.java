package ru.mrflaxe.betterarrows;

import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.betterarrows.listener.ArrowEventListener;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        new ArrowEventListener(this).register();
    }
}