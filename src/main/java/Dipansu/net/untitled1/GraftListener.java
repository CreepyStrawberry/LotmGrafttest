package Dipansu.net.untitled1;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class GraftListener implements Listener {

    private static final Material ITEM = Material.STICK;
    private final LotmPlugin plugin;
    private final GraftManager manager;

    public GraftListener(LotmPlugin plugin, GraftManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private boolean eligible(Player p, PlayerData d) {
        if (d.sequence > 1) {
            p.sendMessage(Component.text("You lack the Sequence for Grafting. (/sequence 1)", NamedTextColor.GRAY));
            return false;
        }
        return true;
    }




    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() != ITEM) return;
        e.setCancelled(true);

        PlayerData d = plugin.data(p);
        if (!eligible(p, d)) return;
        pick(p, d, e.getRightClicked());
    }


    @EventHandler
    public void onAirClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR) return;
        Player p = e.getPlayer();
        if (!p.isSneaking()) return;
        if (p.getInventory().getItemInMainHand().getType() != ITEM) return;

        PlayerData d = plugin.data(p);
        if (!eligible(p, d)) return;
        pick(p, d, p);
    }

    private void pick(Player p, PlayerData d, Entity clicked) {
        if (d.firstTarget == null) {
            d.firstTarget = clicked.getUniqueId();
            p.sendMessage(Component.text("Source chosen. Pick the destination.", NamedTextColor.GRAY));
            return;
        }
        Entity first = Bukkit.getEntity(d.firstTarget);
        d.firstTarget = null;
        if (first == null || first.isDead()) {
            p.sendMessage(Component.text("The source no longer exists.", NamedTextColor.RED));
            return;
        }
        manager.cast(p, d, first, clicked);
    }




    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        UUID id = e.getEntity().getUniqueId();
        GraftManager.Redirect r = manager.redirects.get(id);
        if (r == null || manager.guard.contains(id)) return;

        if (System.currentTimeMillis() > r.expiresAt()) {
            manager.redirects.remove(id);
            return;
        }

        Entity dst = Bukkit.getEntity(r.target());
        if (!(dst instanceof LivingEntity victim) || victim.isDead()) {
            manager.redirects.remove(id);
            return;
        }

        double dmg = e.getFinalDamage();
        e.setCancelled(true);

        manager.guard.add(id);
        manager.guard.add(r.target());
        try {
            victim.damage(dmg);
        } finally {
            manager.guard.remove(id);
            manager.guard.remove(r.target());
        }
        victim.getWorld().spawnParticle(Particle.SOUL, victim.getLocation().add(0, 1, 0),
                20, 0.3, 0.5, 0.3, 0.02);
    }




    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFatalDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity source)) return;

        UUID id = source.getUniqueId();
        GraftManager.Redirect r = manager.deathGrafts.get(id);
        if (r == null) return;

        if (System.currentTimeMillis() > r.expiresAt()) {
            manager.deathGrafts.remove(id);
            return;
        }


        if (e.getFinalDamage() < source.getHealth()) return;

        Entity dst = Bukkit.getEntity(r.target());
        if (!(dst instanceof LivingEntity decoy) || decoy.isDead()) {
            manager.deathGrafts.remove(id);
            return;
        }

        manager.deathGrafts.remove(id);
        e.setCancelled(true);


        AttributeInstance max = source.getAttribute(Attribute.MAX_HEALTH);
        double maxHp = max != null ? max.getValue() : 20;
        source.setHealth(Math.max(1, maxHp * 0.3));


        decoy.setHealth(0);

        source.getWorld().playSound(source.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 0.7f);
        source.getWorld().spawnParticle(Particle.SOUL, source.getLocation().add(0, 1, 0),
                40, 0.4, 0.8, 0.4, 0.03);
        decoy.getWorld().spawnParticle(Particle.SOUL, decoy.getLocation().add(0, 1, 0),
                40, 0.4, 0.8, 0.4, 0.03);
    }
}