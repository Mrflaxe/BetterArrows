package ru.mrflaxe.betterfirearrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fire;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class Listener implements org.bukkit.event.Listener{
    
    private final JavaPlugin plugin;
    private final List<Integer> activeArrows;
    
    public Listener(JavaPlugin plugin) {
        this.plugin = plugin;
        
        activeArrows = new ArrayList<>();
    }
    
    @EventHandler
    public void onBowShoot(EntityShootBowEvent e) {
        ItemStack bow = e.getBow();
        
        if(bow.containsEnchantment(Enchantment.ARROW_FIRE))
            e.getProjectile().setMetadata("inflammatory", new FixedMetadataValue(plugin, true));
    }
    
    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        if(e.getEntityType() != EntityType.ARROW) return;
        Arrow arrow = (Arrow) e.getEntity();
        
        if(!arrow.hasMetadata("inflammatory")) return;
        
        
        if(e.getHitEntity() != null && e.getHitEntity().getType() == EntityType.CREEPER) {
            Creeper creeper = (Creeper) e.getHitEntity();
            creeper.setTarget(creeper);
            return;
        }
        
        Block hitBlock = e.getHitBlock();
        if(hitBlock == null) return;
        if(!hitBlock.getBlockData().getMaterial().isBurnable()) return;
        
        BlockFace hitFace = e.getHitBlockFace();
        Block nextBlock = hitBlock.getRelative(hitFace);
        
        if(hitFace == null) return;
        
        // Little randomization
        Random r = new Random();
        if(r.nextBoolean()) return;
        
        int arrowId = arrow.getEntityId();
        activeArrows.add(arrowId);
        System.out.println("ƒобавл€ем стрелу в список");
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!activeArrows.contains(arrowId)) {
                System.out.println("Id стрелы не найден в спике - return");
                return;
            }
            
            activeArrows.remove((Object) arrowId);
            
            nextBlock.setType(Material.FIRE);
            Fire fireData = (Fire) nextBlock.getBlockData();
            
            BlockFace relativeFace = nextBlock.getFace(hitBlock);
            if(!fireData.getAllowedFaces().contains(relativeFace)) return;
            
            fireData.setFace(relativeFace, true);
            nextBlock.setBlockData(fireData);
        }, (r.nextInt(15) + 1) * 20);
    }
    
    @EventHandler
    public void onArrowPickUp (PlayerPickupArrowEvent e) {
        AbstractArrow arrow = e.getArrow();
        int arrowId = arrow.getEntityId();

        if(activeArrows.contains(arrowId)) {
            activeArrows.remove((Object)arrowId);
        }
    }
    
    @EventHandler
    public void onEntityDamage (EntityDamageEvent e) {
        if(e.getEntityType() != EntityType.CREEPER) return;
        
        DamageCause cause = e.getCause();
        if(cause != DamageCause.FIRE_TICK) return;
        
        Creeper creeper = (Creeper) e.getEntity();
        Random r = new Random();
        
        int chance = r.nextInt(5);
        if(chance != 1) return;
           
        creeper.setTarget(creeper);
    }
    
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}