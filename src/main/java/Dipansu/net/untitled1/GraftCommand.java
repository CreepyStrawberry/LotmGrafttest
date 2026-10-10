package Dipansu.net.untitled1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class GraftCommand implements CommandExecutor, TabCompleter {

    private final LotmPlugin plugin;

    public GraftCommand(LotmPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        PlayerData d = plugin.data(p);

        if (cmd.getName().equalsIgnoreCase("sequence")) {
            if (args.length != 1) return false;
            try {
                int s = Integer.parseInt(args[0]);
                if (s < 0 || s > 9) return false;
                d.sequence = s;
                p.sendMessage("§6Sequence set to " + s);
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        }

        if (args.length != 1) return false;
        try {
            d.mode = GraftMode.valueOf(args[0].toUpperCase(Locale.ROOT));
            p.sendMessage("§6Mode: " + d.mode.display);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 1 && c.getName().equalsIgnoreCase("graft")) {
            return List.of("damage", "swap", "death");
        }
        return List.of();
    }
}