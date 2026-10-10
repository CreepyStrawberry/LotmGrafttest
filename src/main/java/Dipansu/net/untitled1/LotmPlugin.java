package Dipansu.net.untitled1;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LotmPlugin extends JavaPlugin {

    private final Map<UUID, PlayerData> data = new HashMap<>();

    public PlayerData data(Player p) {
        return data.computeIfAbsent(p.getUniqueId(), id -> new PlayerData());
    }

    @Override
    public void onEnable() {
        GraftManager manager = new GraftManager();
        getServer().getPluginManager().registerEvents(new GraftListener(this, manager), this);

        GraftCommand cmd = new GraftCommand(this);
        getCommand("graft").setExecutor(cmd);
        getCommand("graft").setTabCompleter(cmd);
        getCommand("sequence").setExecutor(cmd);

        // Spirituality regeneration and action bar, once per second
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData d = data(p);
                d.spirituality = Math.min(d.maxSpirituality, d.spirituality + 2);
                p.sendActionBar(Component.text(
                        "Spirituality: " + (int) d.spirituality + "/" + (int) d.maxSpirituality
                                + "  |  " + d.mode.display + "  |  Seq " + d.sequence,
                        NamedTextColor.GOLD));
            }
        }, 20L, 20L);
    }
}