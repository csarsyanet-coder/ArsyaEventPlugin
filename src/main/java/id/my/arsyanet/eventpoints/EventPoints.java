package id.my.arsyanet.eventpoints;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventPoints extends JavaPlugin implements CommandExecutor {

    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        createDataFile();
        
        if (getCommand("point") != null) {
            getCommand("point").setExecutor(this);
        }

        // Hook ke PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EventPointsExpansion(this).register();
            getLogger().info("PlaceholderAPI ditemukan! Placeholder %eventpoints_amount% berhasil didaftarkan.");
        } else {
            getLogger().warning("PlaceholderAPI tidak ditemukan! Fitur scoreboard tidak akan berfungsi.");
        }

        getLogger().info("EventPoints berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info("EventPoints dinonaktifkan!");
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Gagal membuat file data.yml!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Gagal menyimpan data poin!");
            e.printStackTrace();
        }
    }

    // Harus PUBLIC agar bisa diakses oleh EventPointsExpansion
    public int getPoints(String playerName) {
        return dataConfig.getInt("points." + playerName.toLowerCase(), 0);
    }

    private void setPoints(String playerName, int amount) {
        dataConfig.set("points." + playerName.toLowerCase(), amount);
        saveData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Gunakan: /point <add|remove|reset|top|cek>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "cek":
                String targetCheck = args.length > 1 ? args[1] : sender.getName();
                int currentPoints = getPoints(targetCheck);
                sender.sendMessage(ChatColor.GREEN + "Point " + targetCheck + ": " + ChatColor.WHITE + currentPoints);
                break;

            case "add":
                if (!sender.hasPermission("eventpoints.admin")) {
                    sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki akses!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Gunakan: /point add <player> <jumlah>");
                    return true;
                }
                try {
                    int addAmount = Integer.parseInt(args[2]);
                    String targetAdd = args[1];
                    setPoints(targetAdd, getPoints(targetAdd) + addAmount);
                    sender.sendMessage(ChatColor.GREEN + "Berhasil menambahkan " + addAmount + " point kepada " + targetAdd);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Jumlah point harus berupa angka!");
                }
                break;

            case "remove":
                if (!sender.hasPermission("eventpoints.admin")) {
                    sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki akses!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Gunakan: /point remove <player> <jumlah>");
                    return true;
                }
                try {
                    int removeAmount = Integer.parseInt(args[2]);
                    String targetRemove = args[1];
                    int newPoints = Math.max(0, getPoints(targetRemove) - removeAmount); 
                    setPoints(targetRemove, newPoints);
                    sender.sendMessage(ChatColor.GREEN + "Berhasil mengurangi " + removeAmount + " point dari " + targetRemove);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Jumlah point harus berupa angka!");
                }
                break;

            case "reset":
                if (!sender.hasPermission("eventpoints.admin")) {
                    sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki akses!");
                    return true;
                }
                dataConfig.set("points", null);
                saveData();
                sender.sendMessage(ChatColor.YELLOW + "Semua data point event telah di-reset menjadi 0!");
                break;

            case "top":
                if (dataConfig.getConfigurationSection("points") == null || dataConfig.getConfigurationSection("points").getKeys(false).isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Belum ada data point yang tercatat.");
                    return true;
                }

                Map<String, Integer> leaderboard = new HashMap<>();
                for (String key : dataConfig.getConfigurationSection("points").getKeys(false)) {
                    leaderboard.put(key, dataConfig.getInt("points." + key));
                }

                sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Top 10 Point Event" + ChatColor.GOLD + " ===");
                leaderboard.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(10)
                        .forEach(entry -> sender.sendMessage(ChatColor.AQUA + entry.getKey() + ": " + ChatColor.WHITE + entry.getValue() + " Point"));
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Command tidak ditemukan. Gunakan: /point <add|remove|reset|top|cek>");
                break;
        }
        return true;
    }
}