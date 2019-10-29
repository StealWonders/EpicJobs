package de.stealwonders.epicjobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Utils {

    public static String serializeLocation(Location location) {
        return location.getWorld() + " " + location.getX() + " " + location.getY() + " " + location.getZ();
    }

    public static Location deserializeLocation(String input) {
        String[] parts = input.split(" ");
        World world = Bukkit.getWorld(parts[0]);
        if (world != null) {
            return new Location(world, Double.valueOf(parts[1]), Double.valueOf(parts[2]), Double.valueOf(parts[3]));
        } else {
            return null;
        }
    }

}
