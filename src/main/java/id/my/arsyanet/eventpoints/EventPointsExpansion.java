package id.my.arsyanet.eventpoints;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class EventPointsExpansion extends PlaceholderExpansion {

    private final EventPoints plugin;

    public EventPointsExpansion(EventPoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "eventpoints";
    }

    @Override
    public String getAuthor() {
        return "arsya dev";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; 
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("amount")) {
            return String.valueOf(plugin.getPoints(player.getName()));
        }

        return null;
    }
}