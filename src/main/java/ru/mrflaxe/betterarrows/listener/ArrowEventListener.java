package ru.mrflaxe.betterarrows.listener;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ArrowEventListener implements org.bukkit.event.Listener{
    
    private final JavaPlugin plugin;
    
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if(event.getEntityType() != EntityType.ARROW) {
            return;
        }
        
        Arrow arrow = (Arrow) event.getEntity();
        
        if(arrow.getFireTicks() != 0) {
            handleFireArrowHit(event);
        }
        
        handleDefaultArrowHit(event);
    }
    
    private void handleFireArrowHit(ProjectileHitEvent event) {
        Arrow arrow = (Arrow) event.getEntity();
        
        // If fired arrow hit a creeper it will explode!
        if(event.getHitEntity() != null && event.getHitEntity().getType() == EntityType.CREEPER) {
            Creeper creeper = (Creeper) event.getHitEntity();
            creeper.setTarget(creeper);
            return;
        }
        
        // Or if arrow hit a burnable block it will with some chance settled on fire
        Block hitBlock = event.getHitBlock();
        
        if(hitBlock == null) {
            return;
        }
        
        // If block is not burnable like a stone bricks
        if(!hitBlock.getBlockData().getMaterial().isBurnable()) {
            return;
        }
        
        BlockFace hitFace = event.getHitBlockFace();
        Block nextBlock = hitBlock.getRelative(hitFace);
        
        if(hitFace == null) {
            return;
        }
        
        // Little randomization 50/50 to set a block on fire
        Random r = new Random();
        boolean willBurn = r.nextBoolean();
        
        if(!willBurn) {
            return;
        }
        
        // If will Burn is true set delayed task to set it on fire
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            
            // If someone pickup the arrow
            if(arrow.isDead()) {
                return;
            }
            
            // If arrow is no longer in fire
            if(arrow.getFireTicks() == 0) {
                return;
            }
            
            // Can set the fire only in empty blocks
            if(!nextBlock.getType().equals(Material.AIR)) {
                return;
            }
            
            nextBlock.setType(Material.FIRE);
            Fire fireData = (Fire) nextBlock.getBlockData();
            
            BlockFace relativeFace = nextBlock.getFace(hitBlock);
            if(!fireData.getAllowedFaces().contains(relativeFace)) return;
            
            fireData.setFace(relativeFace, true);
            nextBlock.setBlockData(fireData);
        }, (r.nextInt(15) + 1) * 20);
    }
    
    private void handleDefaultArrowHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();
        
        // If an arrow hits primed tnt it will instantly explode
        if(hitEntity != null && hitEntity.getType().equals(EntityType.PRIMED_TNT)) {
            TNTPrimed primedTNT = (TNTPrimed) hitEntity;
            primedTNT.setFuseTicks(1);
        }
    }
    
    @EventHandler
    public void onEntityDamage (EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.CREEPER) {
            return;
        }
        
        // If creeper gets fire damage
        // there is a chance 10% that it will suddenly explode.
        DamageCause cause = event.getCause();
        if(cause != DamageCause.FIRE_TICK) {
            return;
        }
        
        Creeper creeper = (Creeper) event.getEntity();
        Random r = new Random();
        
        int chance = r.nextInt(10);
        if(chance != 1) return;
           
        creeper.setTarget(creeper);
    }
    
    @EventHandler
    public void onBottleSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        Collection<PotionEffect> effects = potion.getEffects();
        
        // Filltering for water splash potions
        if(effects.size() > 0) {
            return;
        }
        
        Location hitLocation = event.getEntity().getLocation();
        Collection<Entity> affectedEntities = hitLocation.getWorld().getNearbyEntities(hitLocation, 2, 0.5, 2);
        
        // If splash gets a fired arrow will make it no longer in fire
        for (Entity entity : affectedEntities) {
            EntityType type = entity.getType();
            
            if(!type.equals(EntityType.ARROW)) {
                return;
            }
            
            Arrow arrow = (Arrow) entity;
            boolean isFiredArrow = arrow.getFireTicks() != 0;
            
            if(isFiredArrow) {
                arrow.setFireTicks(0);
            }
        }
    }
    
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}