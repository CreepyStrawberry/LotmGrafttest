package Dipansu.net.untitled1;


import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GraftManager {

    public record Redirect(UUID target, long expiresAt) {}


    public final Map<UUID, Redirect> redirects = new HashMap<>();

    public final Map<UUID, Redirect> deathGrafts = new HashMap<>();

    public final Set<UUID> guard = new HashSet<>();

    private long durationMs(PlayerData d) { return d.sequence == 0 ? 20_000 : 10_000; }
    private double range(PlayerData d)    { return d.sequence == 0 ? 50 : 30; }

    public void cast(Player caster, PlayerData d, Entity a, Entity b) {
        if (a.equals(b)) {
            caster.sendMessage("§7You can't graft something onto itself.");
            return;
        }
        if (System.currentTimeMillis() - d.lastCast < 1000) return;

        double cost = d.sequence == 0 ? d.mode.cost * 0.7 : d.mode.cost;
        if (d.spirituality < cost) {
            caster.sendMessage("§cNot enough Spirituality.");
            return;
        }

        double r = range(d);
        if (!a.getWorld().equals(caster.getWorld()) || !b.getWorld().equals(caster.getWorld())
                || a.getLocation().distance(caster.getLocation()) > r
                || b.getLocation().distance(caster.getLocation()) > r) {
            caster.sendMessage("§cTarget is too far.");
            return;
        }

        switch (d.mode) {
            case DAMAGE -> redirects.put(a.getUniqueId(),
                    new Redirect(b.getUniqueId(), System.currentTimeMillis() + durationMs(d)));

            case SWAP -> swap(a, b);

            case DEATH -> {
                if (!(a instanceof LivingEntity) || !(b instanceof LivingEntity)) {
                    caster.sendMessage("§cBoth targets must be living.");
                    return;
                }
                deathGrafts.put(a.getUniqueId(),
                        new Redirect(b.getUniqueId(), System.currentTimeMillis() + durationMs(d) * 3));
            }
        }

        d.spirituality -= cost;
        d.lastCast = System.currentTimeMillis();
        drawThread(a.getLocation().add(0, 1, 0), b.getLocation().add(0, 1, 0));
        caster.playSound(caster.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 0.6f);
    }


    private void swap(Entity a, Entity b) {
        Location la = a.getLocation().clone();
        Location lb = b.getLocation().clone();
        a.teleport(lb);
        b.teleport(la);
    }


    private void drawThread(Location from, Location to) {
        World w = from.getWorld();
        if (w == null || !w.equals(to.getWorld())) return;

        Vector dir = to.toVector().subtract(from.toVector());
        double len = dir.length();
        if (len == 0) return;
        dir.normalize();

        Particle.DustOptions grey = new Particle.DustOptions(Color.fromRGB(150, 150, 150), 1.4f);
        for (double i = 0; i < len; i += 0.4) {
            Location p = from.clone().add(dir.clone().multiply(i));
            w.spawnParticle(Particle.DUST, p, 1, 0, 0, 0, 0, grey);
        }
    }
}